from Schedule import *
from Constants import *
from pprint import *
from Exceptions import *
import copy


# generates all possible assignments for the given activity to the schedule within the given threshold
def generatePartialSchedules(schedule: Schedule, activity: Activity, threshold):
    ret = []
    if activity.withTrainer:
        possibleStarts = schedule.trainerTable.calcPossibleStarts(activity)
    else:
        possibleStarts = schedule.getAthleteTable(
            activity.athlete
        ).fuzzyCalcPossibleStarts(activity)

    for day, startTimes in possibleStarts:
        if not schedule.getAthleteTable(activity.athlete).getDay(day).isFree():
            continue
        
        # this enforces the intensity constraint
        if day > 0:
            if not (
                schedule.getAthleteTable(activity.athlete)
                .getDay(day - 1)
                .canBeFollowedUpBy(activity)
            ):
                continue

        for startTime in startTimes:
            tmp = schedule.myDeepcopy()
            tmp.assignActivity(activity, day, startTime)
            score = scoringFunction(tmp)
            if score >= threshold:
                ret.append(tmp)
    return ret




# procedurally generates full schedules by extending schedules with new partial assignments
def generateFullSchedules(schedule: Schedule, activities: List[Activity], threshold):
    ret = []
    _activities = copy.deepcopy(activities)
    next = _activities.pop()
    tmp = generatePartialSchedules(schedule, next, threshold)
    if len(_activities) > 0:
        for t in tmp:
            ret.extend(generateFullSchedules(t, _activities, threshold))
            if len(ret) >= 1:
                return ret
        return ret
    return tmp


# calculates a score for a given TimeTable for all activities
def tableScoringFunction(timeTable:TimeTable):
    score = 0
    for day in timeTable.weekDays:
        for slot in day.slots:
            if slot.hasActicity():
                if not slot.wasFree():
                    score = score - (MAX_ACTIVITY_DUARION / slot.getActivity().duration)
    return score    

# calculates a score for a given assignment (schedule)
def scoringFunction(schedule:Schedule):
    score = 0
    for at_key in schedule.athleteTables.keys():
        score = score + tableScoringFunction(schedule.athleteTables.get(at_key))
    return score


# makes sure the given list of activities can even be schedule given the
# intensity constraint 
def checkIntensities(activities: List[Activity]):
    athletes = {}
    for a in activities:
        if a.athlete not in athletes:
            athletes[a.athlete] = []
        athletes[a.athlete].append(a)

    for a in athletes:
        # check if too many highs
        high_cnt = 0
        for activity in athletes[a]:
            if activity.intensity == 2:
                high_cnt += 1
        if high_cnt > 4:
            raise IntensityConstraintException("Too many ativities with intensity HARD", a)

        #
        sum = 0
        for activity in athletes[a]:
            sum += activity.intensity
        if sum > 8:
            raise IntensityConstraintException(
                "Too much overall intensity over the week", a
            )

# helper function for the checkTrainerTable
def generatePartialTimeTable(timeTable: TimeTable, activity: Activity):
    ret = []
    possibleStarts = timeTable.calcPossibleStarts(activity)
    for day, startTimes in possibleStarts:
        for startTime in startTimes:
            tmp = timeTable.myDeepCopy()
            tmp.assignActivity(activity, day, startTime)
            ret.append(tmp)
    return ret

# helper function for the checkTrainerTable
# this is basically the same algortihm as the "main algorithm" but 
# it only runs on a single timetable instead of a full schedule
def generateFullTimeTable(timeTable: TimeTable, activities: List[Activity]):
    ret = []
    _activities = copy.deepcopy(activities)
    next = _activities.pop()
    tmp = generatePartialTimeTable(timeTable, next)
    if len(_activities) > 0:
        for t in tmp:
            ret.extend(generateFullTimeTable(t, _activities))
            if len(ret) >= 1:
                return ret
        return ret
    return tmp

# checks if all the activities that require the trainer to be present
# can even be scheduled into the trainers timetable (ignoring the athlete tables)
def checkTrainerTable(timeTable: TimeTable, activities: List[Activity]):
    activities_with_trainer = []
    for a in activities:
        if a.withTrainer:
            activities_with_trainer.append(a)
    if activities_with_trainer != []:
        ret = generateFullTimeTable(timeTable, activities_with_trainer)
        if ret == []:
            raise TrainerConstraintExcpetion(
                "Not solveable due to trainer hardconstraints",
                timeTable,
                activities_with_trainer,
            )


# "main solving algorithm"
# calles generateFullSchelue with increasingly "bad" threshold until a solution is found
def scheduleThresholdSearch(
    activities: List[Activity], schedule: Schedule, stepwidth=1
):
    _activities = sorted(
        activities,
        key=lambda a: (
            a.withTrainer,
            a.intensity,
            a.duration,
        ),
        reverse=False,
    )
    # make sure there isnt too much intensity over the activities
    checkIntensities(_activities)
    # make sure the activites with trainer can even be fit into the trainers timetable
    checkTrainerTable(schedule.trainerTable, _activities)
    r = []
    threshold = 0
    while True:
        r = generateFullSchedules(
            schedule=schedule, activities=_activities, threshold=threshold
        )
        if len(r) > 0:
            break
        threshold -= stepwidth
    return (threshold,r)


def main():
    pass


if __name__ == "__main__":
    main()
