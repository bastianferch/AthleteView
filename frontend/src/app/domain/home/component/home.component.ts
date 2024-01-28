import { Component, OnInit } from '@angular/core';
import { CalendarView } from "angular-calendar";
import { ActivityType, PlannedActivity } from "../../activity/dto/PlannedActivity";
import { ActivityService } from "../../activity/service/activity.service";
import { AuthService } from "../../auth/service/auth.service";
import { differenceInHours, subDays } from "date-fns";
import { toInteger } from "lodash";
import { HealthService } from "../../health/service/health.service";
import { Health } from "../../../common/dto/Health";
import { SnackbarService } from "../../../common/service/snackbar.service";
import { Activity } from "../../activity/dto/Activity";
import { TrainingsplanService } from "../../trainingsplan/service/trainingsplan.service";
import { User } from "../../trainingsplan/dto/user";
import { ActivityParsing } from "../../../common/util/parsing/activity-parsing";
import { FitnessService } from "../../fitness/service/fitness.service";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
})
export class HomeComponent implements OnInit {

  protected readonly CalendarView = CalendarView;
  upcoming: PlannedActivity[] = []
  plannedActivities: PlannedActivity[] = []
  pastActivities: Activity[] = []
  past: ActivityType[] = []
  activityStats: Map<ActivityType, number> = new Map()
  athletes: User[]
  currentAthlete: number
  health: Health = { avgBPM: -1, avgSteps: -1, avgSleepDuration: -1 }
  fitness: number[];
  isTrainer = false
  uid: number

  constructor(
    private activityService: ActivityService,
    private authService: AuthService,
    private fitnessService: FitnessService,
    private healthService: HealthService,
    private snackbarService: SnackbarService,
    private trainingsPlanService: TrainingsplanService,
    public actParser: ActivityParsing,
  ) {}

  ngOnInit() {
    this.uid = this.authService.currentUser.id
    this.isTrainer = this.authService.currentUser.userType === 'trainer'
    const date = new Date()
    this.activityService.fetchAllPlannedActivitiesForUser(this.uid, date.toISOString(), null).subscribe({
      next: (data) => {
        this.plannedActivities = data.sort((a, b) => {
          const aDate = a.date as Date
          const bDate = b.date as Date
          return aDate.valueOf() - bDate.valueOf()
        })
        if (this.isTrainer) this.upcoming = this.plannedActivities.filter((a) => a.withTrainer).slice(0,4)
        else this.upcoming = this.plannedActivities.slice(0,4)
      },
      error: (error) => this.snackbarService.openSnackBar(error.error.message),
    })
    this.activityService.fetchAllActivitiesForUser(this.uid, subDays(date,7).toISOString(), date.toISOString()).subscribe({
      next: (data) => {
        this.pastActivities = data
        this.calcStats()
      },
      error: (error) => this.snackbarService.openSnackBar(error.error.message),
    })
    if (this.isTrainer) {
      this.trainingsPlanService.fetchAthletesForTrainer().subscribe({
        next: (athletes) => {
          this.athletes = athletes
          if (athletes.length > 0) {
            this.currentAthlete = 0
            this.getHealthAndFitnessForAthlete()
          }
        },
        error: (error) => this.snackbarService.openSnackBar(error.error?.message),
      })
    } else {
      this.getFitness(this.uid);
      this.healthService.get().subscribe({
        next: (data) => this.health = data,
        error: (error) => this.snackbarService.openSnackBar(error.error?.message),
      })
    }
  }

  calcStats() {
    let list = this.pastActivities
    if (this.isTrainer) {
      if (this.currentAthlete === undefined) return
      list = list.filter((a) => a.athleteId === this.athletes[this.currentAthlete].id)
      this.activityStats.clear()
    }
    for (const a of list) {
      let meters = this.activityStats.get(a.activityType) ? this.activityStats.get(a.activityType) : 0
      meters += a.distance
      this.activityStats.set(a.activityType, meters)
    }
    this.past = [...this.activityStats.keys()]
  }

  getTimeForActivity(date: any): string {
    const now = new Date()
    const hours = differenceInHours(date, now)
    if (hours < 24) {
      return `in ${hours} hour${hours === 1 ? '' : 's'}`
    }
    return `in ${toInteger(hours / 24)} day${toInteger(hours / 24) === 1 ? '' : 's'} and ${hours % 24} hour${(hours % 24) === 1 ? '' : 's'}`
  }

  prevUser() {
    this.currentAthlete += (this.athletes.length - 1)
    this.currentAthlete %= this.athletes.length
    this.getHealthAndFitnessForAthlete()
    this.calcStats()
  }

  nextUser() {
    this.currentAthlete += 1
    this.currentAthlete %= this.athletes.length
    this.getHealthAndFitnessForAthlete()
    this.calcStats()
  }

  getHealthAndFitnessForAthlete() {
    this.healthService.getFromAthlete(this.athletes[this.currentAthlete].id).subscribe({
      next: (data) => this.health = data,
      error: (error) => this.snackbarService.openSnackBar(error.error?.message),
    })
    this.getFitness(this.athletes[this.currentAthlete].id);
  }

  private getFitness(targetUserId: number): void {
    this.fitnessService.getFitness(targetUserId)
      .subscribe({ next: (data) => {
        data.reverse()
        this.fitness = data.map((x: number) => (x < 0 ? 0 : x))
      },
      error: (error) => this.snackbarService.openSnackBar(error.error?.message) },
      );
  }

}
