import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivityService } from '../service/activity.service';
import { AuthService } from '../../auth/service/auth.service';
import { Activity } from '../dto/Activity';
import { PlannedActivity } from '../dto/PlannedActivity';
import { MatTable } from '@angular/material/table';
import { SpinnerService } from '../../main/service/spinner.service';
import { IntervalSplit } from 'src/app/common/interval/dto/Interval';
import { StepDurationType } from 'src/app/common/interval/dto/Step';
import { format } from 'date-fns';
import { ActivityParsing } from "../../../common/util/parsing/activity-parsing";

export enum TOGGLESTATE {
  DRAFT = "draft",
  DONE = "done"
}

@Component({
  selector: 'app-activity',
  templateUrl: './activity.component.html',
  styleUrls: ['./activity.component.scss'],
})
export class ActivityComponent implements OnInit {
  @ViewChild(MatTable) table: MatTable<any>;

  // default view columns
  private doneColumns = ['finishedActivityIcon', 'finishedActivityReadableName', 'finishedActivityAccuracy', 'finishedActivityDistance', 'finishedActivityDates']
  private draftColumns = ['activityIcon', 'activityName', 'readableName', 'activitySummary', 'activityCreatedFor', 'activityDate'];

  columnsToDisplay = this.draftColumns
  viewDataSet: object[] = []

  finishedActivities: Activity[] = []
  plannedActivities: PlannedActivity[] = []
  toggleState: string = TOGGLESTATE.DRAFT
  TOGGLESTATE_ENUM = TOGGLESTATE // used in html
  dataLoaded = false

  private partialLoad = false
  private uid: number

  constructor(
    private activityService: ActivityService,
    private authService: AuthService,
    private spinner: SpinnerService,
    public activityParsing: ActivityParsing,
  ) {}

  ngOnInit(): void {
    // load init data
    this.spinner.addActiveRequest()
    this.uid = this.authService.currentUser.id
    this.activityService.fetchAllActivitiesForUser(this.uid, null, null).subscribe((data) => {
      this.finishedActivities = data
      if (this.partialLoad) {
        this.spinner.removeActiveRequest()
        this.dataLoaded = true
      }
      this.partialLoad = true
    })
    this.activityService.fetchAllPlannedActivitiesForUser(this.uid, null, null).subscribe((data) => {
      this.plannedActivities = data
      this.viewDataSet = data
      if (this.partialLoad) {
        this.spinner.removeActiveRequest()
        this.dataLoaded = true
      }
      this.partialLoad = true
    })
  }

  getDate(activity: PlannedActivity): Date {
    return activity.date as Date
  }

  generateIntervalSummary(interval: IntervalSplit): string {
    if (interval === undefined) {
      return " - " // fail-safe
    }

    let summaryString = ""
    let repeatFlag = false

    if (interval.repeat !== null && interval.repeat > 1) {
      repeatFlag = true
      summaryString += `${interval.repeat}x `
    }

    if (interval.step !== null) {
      const step = interval.step
      switch (step.durationType) {
        // TODO: add warmup/recovery/...?
        case StepDurationType.DISTANCE:
          summaryString += `${step.duration} ${step.durationUnit.toLowerCase()} `
          break
        case StepDurationType.LAPBUTTON:
          summaryString += `1 lap `
          break
        case StepDurationType.TIME:
          summaryString += `${step.duration} ${step.durationUnit.toLocaleLowerCase()}`
          break
        default:
          // shouldn't occur
          break
      }
    }

    if (interval.intervals !== null && interval.intervals.length > 0) {
      const data = interval.intervals.map((item) => this.generateIntervalSummary(item).trim())
      if (repeatFlag && data.length > 1) {
        summaryString += `(${data.join(" + ")})`
      } else {
        summaryString += data.join(" + ")
      }
    }

    return summaryString.trim()
  }

  getRouterLink(activity: any): string {
    let routingPath = ""
    if (this.toggleState === TOGGLESTATE.DRAFT) {
      routingPath = String(activity.id)
    } else {
      routingPath = `finished/${activity.id}`
    }
    return routingPath
  }

  handleViewChange(groupValue: string) {
    this.toggleState = groupValue
    switch (groupValue) {
      case TOGGLESTATE.DONE:
        this.columnsToDisplay = this.doneColumns
        this.viewDataSet = this.finishedActivities
        break
      case TOGGLESTATE.DRAFT:
        this.columnsToDisplay = this.draftColumns
        this.viewDataSet = this.plannedActivities
        break
      default:
        // should not occur
        break
    }
  }

  generateDistanceString(distance: number): string {
    if (distance > 1000) {
      return `${(distance / 1000).toFixed(1)} km`
    }

    return `${distance} m`
  }

  generateDateRangeString(activity: Activity): string {
    let dateStr = ""
    if (activity.startTime !== undefined) {
      dateStr += format(new Date(activity.startTime as Date), "dd.MM.yyyy, HH:mm")
    }

    if (activity.endTime !== undefined) {
      if (dateStr !== "") {
        dateStr += " - "
      }
      dateStr += format(new Date(activity.endTime as Date), "HH:mm")
    }

    return dateStr
  }
}
