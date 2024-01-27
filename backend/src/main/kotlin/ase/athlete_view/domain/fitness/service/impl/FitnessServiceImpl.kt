package ase.athlete_view.domain.fitness.service.impl

import ase.athlete_view.common.exception.entity.ForbiddenException
import ase.athlete_view.domain.activity.pojo.entity.Activity
import ase.athlete_view.domain.activity.service.ActivityService
import ase.athlete_view.domain.fitness.service.FitnessService
import ase.athlete_view.domain.health.service.HealthService
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.Trainer
import ase.athlete_view.domain.user.pojo.entity.User
import ase.athlete_view.domain.user.service.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class FitnessServiceImpl(
    private val healthService: HealthService,
    private val activityService: ActivityService,
    private val userService: UserService,
) : FitnessService {
    private val logger = KotlinLogging.logger {}
    override fun calculate(byUser: Long, targetUser: Long): List<Int> {
        logger.trace { "S | calculate ($byUser, $targetUser)" }
        val user = this.authorizeAndProvideTargetUser(byUser, targetUser)
        val monthList = mutableListOf<Int>()
        for (i in 0L..4L){
            val now = LocalDate.now().minusDays(i*7)
            val weekAgo = now.minusDays(7).minusDays(i*7)
            val healthList = this.healthService.getAllByUser(targetUser, weekAgo, now)
            val activityList = this.activityService.getAllActivities(
                targetUser, weekAgo.atStartOfDay(), now.atStartOfDay()
            )
            val fitnessList = ArrayList<Int>()
            healthList.forEach { health ->
                val healthFitness = this.calculateHealthFitness(
                    this.calculateAverageHeartHealth(health.avgBPM),
                    this.calculateAverageSleepHealth(health.avgSleepDuration),
                    this.calculateAverageStepsHealth(health.avgSteps)
                )
                val relatedActivities = activityList.filter { activity ->
                    activity.startTime!!.isAfter(health.date.atStartOfDay()) &&
                            activity.endTime!!.isBefore(health.date.plusDays(1).atStartOfDay())
                }
                val activityFitness = this.calculateActivityFitness(
                    relatedActivities,
                    LocalDate.now().year - (user as Athlete).dob.year
                )
                val totalFitness = this.calculateOverallFitness(healthFitness, activityFitness)
                fitnessList.add(totalFitness)
            }
            if (fitnessList.isEmpty()) {
                monthList.add(-1)
            } else{
                monthList.add((fitnessList.reduce { acc, start -> start + acc }) / fitnessList.size)
            }
        }
      return monthList
    }

    private fun authorizeAndProvideTargetUser(byUserId: Long, targetUserId: Long): User {
        val targetUser = this.userService.getById(targetUserId)
        if (targetUser is Trainer) {
            throw ForbiddenException("Cannot provide the fitness for the trainer.")
        }
        if (byUserId == targetUserId) {
            return targetUser
        }
        val byUser: Trainer = this.userService.getById(byUserId) as Trainer
        // Athlete must be in the trainer list.
        byUser.athletes.find { athlete -> athlete.id == targetUserId }
            ?: throw ForbiddenException("Cannot provide the fitness for a provided User.")
        return targetUser
    }

    /**
     * Provides the value between 0 and 100 on how healthy is a person.
     *
     * The proportions are as follows:
     * 1) 70 % of health fitness.
     * 1) 30 % of activity fitness.
     *
     * @param healthFitness is calculated in {@link calculateHealthFitness(heartHealth: Int, sleepHealth: Int, stepsHealth: Int)}.
     * @param activityFitness is calculated in {@link  calculateActivityFitness(activities: List<Activity>, age: Int)}.
     * @return the daily fitness based on Activity data.
     */
    private fun calculateOverallFitness(healthFitness: Int, activityFitness: Int): Int {
        return (healthFitness * 0.7 + activityFitness * 0.3).toInt()
    }

    /**
     * Provides the value between 0 and 100
     * on how healthy is a person based on {@link Activity} class.
     *
     * The proportions are as follows:
     * 1) 100 % of heart activity.
     *
     * @param activities is the daily activities.
     * @param age of an athlete
     * @return the daily fitness based on Activity data.
     * @apiNote the proportions can be adjusted later for more granular fitness calculation.
     */
    private fun calculateActivityFitness(activities: List<Activity>, age: Int): Int {
        if (activities.isEmpty()) {
            return 0
        }
        val avgBPM = (activities
            .map { activity -> activity.averageBpm }
            .reduce { acc, start -> start + acc }) / activities.size
        val maxBPM = activities
            .map { activity -> activity.maxBpm }
            .reduce { acc, start -> if (start > acc) start else acc }
        return calculateHeartActivity(avgBPM, maxBPM, age)
    }

    /**
     * Calculates the fitness of the heart during the activities.
     * Calculations are taken from here[1].
     *
     * @param avgBPM is the average heart beat per minute.
     * @param peakBPM is the maximum heart beat per minute.
     * @param age is the athlete's age.
     * @return the value between 60 and 100 on how well the training was done.
     * [1] https://www.heart.org/en/healthy-living/fitness/fitness-basics/target-heart-rates
     */
    private fun calculateHeartActivity(avgBPM: Int, peakBPM: Int, age: Int): Int {
        var fitness = 100

        fun applyConditions(avgRange: IntRange, peakThreshold: Int) {
            if (avgBPM !in avgRange) {
                fitness -= 20
            }
            if (peakBPM > peakThreshold) {
                fitness -= 20
            }
        }

        when {
            age <= 20 -> applyConditions(100..170, 200)
            age in 20..30 -> applyConditions(95..162, 190)
            age in 30..40 -> applyConditions(90..153, 180)
            age in 40..50 -> applyConditions(85..145, 170)
            age in 50..60 -> applyConditions(80..136, 160)
            age in 60..100 -> applyConditions(75..128, 150)
        }

        return fitness
    }

    /**
     * Provides the value between 0 and 100
     * on how healthy is a person based on {@link Health} class.
     *
     * The proportions are as follows:
     * 1) 30 % of heart health.
     * 2) 50 % of sleep health.
     * 3) 20 % of steps health.
     *
     * @param heartHealth is calculated in {@link calculateAverageHeartHealth(heartBPM: Int)}.
     * @param sleepHealth is calculated in {@link calculateAverageSleepHealth(sleepMinutes: Int)}.
     * @param stepsHealth is calculated in {@link calculateAverageStepsHealth(steps: Int)}.
     * @return the daily fitness based on Health data.
     */
    private fun calculateHealthFitness(heartHealth: Int, sleepHealth: Int, stepsHealth: Int): Int {
        return (heartHealth * 0.3 + sleepHealth * 0.5 + stepsHealth * 0.2).toInt()
    }

    /**
     * Provides the value between 0 and 100 on how healthy is the heart rate during the day.
     * Normal daily heart rate is between 60 and 100 [1].
     * By that professional athlete this value can decrease to 40 [2].
     * Therefore, the 50 is taken as standard.
     * The more extreme is the value, the less score it gets.
     *
     * @param heartBPM of a daily health.
     * @return the daily heart fitness based on Health data.
     *
     * [1] https://www.bhf.org.uk/informationsupport/how-a-healthy-heart-works/your-heart-rate
     * [2] https://www.mayoclinic.org/healthy-lifestyle/fitness/expert-answers/heart-rate/faq-20057979
     */
    private fun calculateAverageHeartHealth(heartBPM: Int): Int {
        if (heartBPM in 51..99) {
            return 100
        }
        if (heartBPM in 40..109) {
            return 80
        }
        if (heartBPM < 119) { // hopefully still alive with a pulse of e.x. 20 BPM :D
            return 50
        }
        return 0
    }

    /**
     * Provides the value between 0 and 100 on how healthy is the sleep for a day.
     * Normal daily sleep rate is between 7 and 9 hours[1][2].
     * The more extreme is the value, the less score it gets.
     *
     * @param sleepMinutes of a daily health.
     * @return the daily sleep fitness based on Health data.
     *
     * [1] https://onlinelibrary.wiley.com/journal/13652869
     * [2] https://www.cdc.gov/sleep/about_sleep/how_much_sleep.html
     */
    private fun calculateAverageSleepHealth(sleepMinutes: Int): Int {
        if (sleepMinutes in 7 * 60..9 * 60) {
            return 100
        }
        if (sleepMinutes in 6 * 60..10 * 60) {
            return 80
        }
        if (sleepMinutes >= 5 * 60) {
            return 60
        }
        if (sleepMinutes >= 4 * 60) {
            return 40
        }
        if (sleepMinutes >= 2 * 60) {
            return 20
        }
        return 0
    }

    /**
     * Provides the value between 0 and 100 on how healthy is the step rate during the day.
     *
     * According to these papers [1,2,3], the goal of 10000 steps per day is a good mark,
     * but not necessarily a proven scientific value. The average of 8000 steps is taken as a goal.
     *
     * @param steps of a daily health.
     * @return the daily steps fitness based on Health data.
     *
     * [1] https://www.sciencedaily.com/releases/2022/03/220303112207.htm
     * [2] https://www.nih.gov/news-events/nih-research-matters/number-steps-day-more-important-step-intensity
     * [3] https://ijbnpa.biomedcentral.com/articles/10.1186/1479-5868-8-79
     */
    private fun calculateAverageStepsHealth(steps: Int): Int {
        if (steps > 8000) {
            return 100
        }
        if (steps > 6000) {
            return 90
        }
        if (steps > 5000) {
            return 80
        }
        if (steps > 4000) {
            return 70
        }
        if (steps > 3000) {
            return 50
        }
        if (steps > 2000) {
            return 30
        }
        if (steps > 1000) {
            return 10
        }
        return 0
    }
}