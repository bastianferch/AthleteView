import { Component, HostListener, OnInit, ViewChild } from '@angular/core';
import { ActivityService } from '../service/activity.service';
import { AuthService } from '../../auth/service/auth.service';
import { Activity } from '../dto/Activity';
import { PlannedActivity } from '../dto/PlannedActivity';
import { MatTable, MatTableDataSource } from '@angular/material/table';
import { SpinnerService } from '../../main/service/spinner.service';
import { IntervalSplit } from 'src/app/common/interval/dto/Interval';
import { StepDurationType } from 'src/app/common/interval/dto/Step';
import { format } from 'date-fns';
import { ActivityParsing } from "../../../common/util/parsing/activity-parsing";
import { MatPaginator } from '@angular/material/paginator';
import { MobileCheckService } from 'src/app/common/service/mobile-checker.service';
import { StyleMapperService } from 'src/app/common/service/style-mapper.service';

export enum TOGGLESTATE {
  DRAFT = "draft",
  DONE = "done",
  TEMPLATE = "template"
}

@Component({
  selector: 'app-activity',
  templateUrl: './activity.component.html',
  styleUrls: ['./activity.component.scss'],
})
export class ActivityComponent implements OnInit {
  @ViewChild(MatTable) table: MatTable<any>;
  @ViewChild(MatPaginator, { static: false })
  set paginator(v: MatPaginator) {
    this.viewDataSet.paginator = v
  }

  // default view columns
  private doneColumns: Array<string>
  private draftColumns: Array<string>
  private templateColumns: Array<string>

  columnsToDisplay: Array<string>
  viewDataSet = new MatTableDataSource<object>()

  finishedActivities: Activity[] = []
  plannedActivities: PlannedActivity[] = []
  templateActivities: PlannedActivity[] = []

  toggleState: string = TOGGLESTATE.DRAFT
  TOGGLESTATE_ENUM = TOGGLESTATE // used in html
  dataLoaded = false
  defaultPageSize = 10
  sortColumn = "activityDate"

  private partialLoad = false
  private uid: number

  constructor(
    private activityService: ActivityService,
    private authService: AuthService,
    private spinner: SpinnerService,
    public activityParsing: ActivityParsing,
    private mobileCheck: MobileCheckService,
    protected styleMapper: StyleMapperService,
  ) {
    this.adjustViewWidth.bind(this)
    this.adjustViewWidth()
  }

  ngOnInit(): void {
    // load init data
    this.spinner.addActiveRequest()
    this.uid = this.authService.currentUser.id
    this.activityService.fetchAllActivitiesForUser(this.uid, null, null).subscribe((data) => {
      this.finishedActivities = data.sort((a, b) => this.compare(a.startTime as Date, b.startTime as Date, false))
      if (this.partialLoad) {
        this.spinner.removeActiveRequest()
        this.dataLoaded = true
      }
      this.partialLoad = true
    })
    this.activityService.fetchAllPlannedActivitiesForUser(this.uid, null, null).subscribe((data) => {
      this.plannedActivities = data.filter((item: PlannedActivity) => item.template === false).sort((a, b) => this.compare(a.date as Date, b.date as Date, false))
      this.templateActivities = data.filter((item: PlannedActivity) => item.template === true).sort((a, b) => this.compare(a.id, b.id, false))

      this.viewDataSet.data = this.plannedActivities
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
    if (this.toggleState === TOGGLESTATE.DRAFT || this.toggleState === TOGGLESTATE.TEMPLATE) {
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
        this.viewDataSet.data = this.finishedActivities
        break
      case TOGGLESTATE.DRAFT:
        this.columnsToDisplay = this.draftColumns
        this.viewDataSet.data = this.plannedActivities
        break
      case this.TOGGLESTATE_ENUM.TEMPLATE:
        this.columnsToDisplay = this.templateColumns
        this.viewDataSet.data = this.templateActivities
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

  isTrainer(): boolean {
    return this.authService.currentUser.isAthlete() === false
  }

  getColumnLen(): number {
    return this.columnsToDisplay.length
  }

  sortData(data: any) {
    const isAsc = data.direction === "asc"

    const sortFun = (a: object, b: object): number => {
      let aAct: Activity | PlannedActivity
      let bAct: Activity | PlannedActivity
      if (this.toggleState === TOGGLESTATE.DRAFT || this.toggleState === TOGGLESTATE.TEMPLATE) {
        aAct = a as PlannedActivity
        bAct = b as PlannedActivity

        switch (data.active) {
          case "readableName":
            return this.compare(this.activityParsing.getReadableNameForActivity(aAct.type), this.activityParsing.getReadableNameForActivity(bAct.type), isAsc)
          case "activityName":
            return this.compare(aAct.name, bAct.name, isAsc)
          case "activityDate":
            return this.compare(aAct.date as Date, bAct.date as Date, isAsc)
          case "":
            return this.compare(aAct.date as Date, bAct.date as Date, false)
        }
      } else if (this.toggleState === TOGGLESTATE.DONE) {
        aAct = a as Activity
        bAct = b as Activity

        switch (data.active) {
          case "finishedActivityReadableName":
            return this.compare(this.activityParsing.getReadableNameForActivity(aAct.activityType), this.activityParsing.getReadableNameForActivity(bAct.activityType), isAsc)
          case "finishedActivityDates":
            return this.compare(aAct.startTime as Date, bAct.startTime as Date, isAsc)
          case "":
            return this.compare(aAct.startTime as Date, bAct.startTime as Date, false)
        }
      }

      return 0
    }

    sortFun.bind(this) // make "this" available in fun
    this.viewDataSet.data = this.viewDataSet.data.sort(sortFun)
  }

  getActivityIconPath(activity: Activity): string {
    return this.styleMapper.getIconPathForActivity(activity)
  }

  getPlannedActivityIconPath(activity: PlannedActivity): string {
    return this.styleMapper.getIconPathForPlannedActivity(activity)
  }

  private compare(a: any, b: any, isAsc: boolean) {
    return (a < b ? -1 : 1) * (isAsc ? 1 : -1)
  }

  @HostListener("window:resize")
  private adjustViewWidth() {
    if (this.mobileCheck.isMobile()) {
      this.doneColumns = ['finishedActivityIcon', 'finishedActivityDistance', 'finishedActivityDates']
      this.draftColumns = ['activityIcon', 'activityName', 'activitySummary', 'activityDate'];
      this.templateColumns = ['activityIcon', 'activityName', 'activitySummary', 'activityEstDuration']
      this.defaultPageSize = 5
    } else {
      this.doneColumns = ['finishedActivityIcon', 'finishedActivityReadableName', 'finishedActivityAccuracy', 'finishedActivityDistance', 'finishedActivityDates']
      this.draftColumns = ['activityIcon', 'activityName', 'readableName', 'activitySummary', 'activityCreatedFor', 'activityDate']
      this.templateColumns = ['activityIcon', 'activityName', 'readableName', 'activitySummary', 'activityIntensity', 'activityEstDuration']
      this.defaultPageSize = 10
    }

    this.handleViewChange(this.toggleState)
  }
}
