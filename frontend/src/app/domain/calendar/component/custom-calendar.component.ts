import { ChangeDetectorRef, Component, Input, OnInit } from '@angular/core';
import { ActivityService } from '../../activity/service/activity.service';
import { AuthService } from '../../auth/service/auth.service';
import { CalendarModule, CalendarEvent, CalendarView } from 'angular-calendar';
import { Activity, ActivityEvent } from '../../activity/dto/Activity'
import { SnackbarService } from 'src/app/common/service/snackbar.service';
import { TimeConstraintService } from '../../time-constraints/service/time-constraints.service'
import { RouterModule, Router, ActivatedRoute } from "@angular/router";
import {
  add,
  addMonths,
  addWeeks,
  endOfMonth,
  endOfWeek,
  format,
  isSameDay,
  isSameMonth,
  startOfMonth,
  startOfWeek,
  subMonths,
  subWeeks,
} from 'date-fns'
import { enUS } from 'date-fns/locale'
import { TimeConstraint } from "../../../common/dto/TimeConstraint";
import { Calendarcolors } from "../../../common/util/calendar-colors";
import { DateParsing, dateFormatString } from "../../../common/util/parsing/date-parsing";
import { PlannedActivityEvent } from '../../activity/dto/PlannedActivity';
import { MatButtonModule } from "@angular/material/button";
import { MatButtonToggleModule } from "@angular/material/button-toggle";
import { NgIf } from "@angular/common";
import { ActivityParsing } from "../../../common/util/parsing/activity-parsing";

@Component({
  selector: 'app-custom-calendar',
  templateUrl: './custom-calendar.component.html',
  styleUrls: ['./custom-calendar.component.scss'],
  imports: [RouterModule, CalendarModule, MatButtonModule, MatButtonToggleModule, NgIf],
  standalone: true,
})
export class CustomCalendarComponent implements OnInit {

  viewDate: Date = new Date()
  @Input() viewType: CalendarView = CalendarView.Month
  @Input() home = false
  // helper to check which view is open atm
  CalendarView = CalendarView
  cropStartHour = 5
  cropEndHour = 22
  events: CalendarEvent[] = []
  activeDayIsOpen = false

  constructor(
    private activityService: ActivityService,
    private activityParsing: ActivityParsing,
    private constraintService: TimeConstraintService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
    private notifService: SnackbarService,
    private router: Router,
    private dateParser: DateParsing,
    private route: ActivatedRoute,
  ) {
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe((params) => {
      const date = new Date(params['date']);
      if (!isNaN(date.getTime())) {
        this.viewDate = date;
      }
    });
    this.loadData()
  }

  handleViewSwitch(view: string) {
    if (view === 'week') {
      // don't need to fetch data
      this.viewType = CalendarView.Week
      this.loadData()
    } else if (view === 'month') {
      this.viewType = CalendarView.Month
      this.loadData()
    }
    this.cdr.detectChanges()
  }

  handleEventClicked(ev: any) {
    if (ev.meta.objectType === "planned") {
      this.router.navigateByUrl(`/activity/${ev.meta.id}`)
    } else {
      this.router.navigateByUrl(`/activity/finished/${ev.meta.id}`)
    }
  }

  handleDayClicked({ date, events }: { date: Date; events: CalendarEvent[] }): void {
    if (isSameMonth(date, this.viewDate)) {
      if (
        (isSameDay(this.viewDate, date) && this.activeDayIsOpen === true) ||
        events.length === 0
      ) {
        this.activeDayIsOpen = false;
      } else {
        this.activeDayIsOpen = true;
      }
      this.viewDate = date;
    }
  }

  getBtnLabelForView(): string {
    if (this.viewType === CalendarView.Week) {
      return "Week";
    }
    return "Month";
  }

  handleDateChange(past: boolean): void {
    if (this.viewType === CalendarView.Month) {
      if (past) {
        this.viewDate = subMonths(this.viewDate, 1)
      } else {
        this.viewDate = addMonths(this.viewDate, 1)
      }
    } else if (past) {
      this.viewDate = subWeeks(this.viewDate, 1)
    } else {
      this.viewDate = addWeeks(this.viewDate, 1)
    }
    this.loadData()
    this.activeDayIsOpen = false
    this.cdr.detectChanges()
  }

  handleDateToday() {
    this.viewDate = new Date()
    this.loadData()
    this.activeDayIsOpen = false
    this.cdr.detectChanges()
  }

  getViewTitle() {
    return format(this.viewDate, "LLLL yyyy", { locale: enUS })
  }

  private loadData() {
    this.events = []

    // load initial calendar-events
    const uid = this.authService.currentUser.id

    let startTime = ""
    let endTime = ""
    if (this.viewType === CalendarView.Week) {
      startTime = format(startOfWeek(this.viewDate, { weekStartsOn: 1 }), dateFormatString)
      endTime = format(endOfWeek(this.viewDate, { weekStartsOn: 1 }), dateFormatString)
    } else {
      startTime = format(startOfMonth(this.viewDate), dateFormatString)
      endTime = format(endOfMonth(this.viewDate), dateFormatString)
    }

    this.activityService.fetchAllActivitiesForUser(uid, startTime, endTime).subscribe({
      next: (data: Array<Activity>) => {
        const calData = data.map((x) => {
          const xEvent: ActivityEvent = { ...x, objectType: "finished" }

          let title = "Finished " + this.activityParsing.getShortNameForActivity(x.activityType)
          if (x.plannedActivity?.createdFor?.id !== uid) title += " - " + x.plannedActivity.createdFor.name
          if (x.accuracy >= 75) {
            return {
              title: title,
              start: x.startTime as Date,
              end: x.endTime as Date,
              resizable: { beforeStart: false, afterEnd: false },
              draggable: false,
              color: Calendarcolors["green"],
              meta: xEvent,
            }
          }
          if (x.accuracy >= 50) {
            return {
              title: title,
              start: x.startTime as Date,
              end: x.endTime as Date,
              resizable: { beforeStart: false, afterEnd: false },
              draggable: false,
              color: Calendarcolors["yellow"],
              meta: xEvent,
            }
          }
          if (x.accuracy >= 25) {
            return {
              title: title,
              start: x.startTime as Date,
              end: x.endTime as Date,
              resizable: { beforeStart: false, afterEnd: false },
              draggable: false,
              color: Calendarcolors["red"],
              meta: xEvent,
            }
          }
          return {
            title: title,
            start: x.startTime as Date,
            end: x.endTime as Date,
            resizable: { beforeStart: false, afterEnd: false },
            draggable: false,
            color: Calendarcolors["blue"],
            meta: xEvent,
          }

        }, this)
        this.events = [...this.events, ...calData]
      },
      error: (e) => {
        this.notifService.openSnackBarWithAction("Error trying to fetch completed activities", "Close")
        console.error(e)
      },
    })

    this.activityService.fetchAllPlannedActivitiesForUser(uid, startTime, endTime).subscribe({
      next: (data) => {
        const calData: CalendarEvent[] = data.map((x) => {
          const xEvent: PlannedActivityEvent = { ...x, objectType: "planned" }
          const activityStart = new Date(x.date as Date)

          // TODO: remove when estimatedDuration is 100% always present
          let est = x.estimatedDuration
          if (est === null || est === undefined) {
            est = 60
          }
          const activityEnd = add(x.date as Date, { minutes: est })
          if (new Date().getTime() >= activityStart.getTime()) {
            return {
              title: x.name + ((x.createdFor?.id !== uid) ? (" - " + x.createdFor.name) : ""),
              start: activityStart,
              end: activityEnd,
              resizable: { beforeStart: false, afterEnd: false },
              draggable: false,
              color: Calendarcolors["red"],
              meta: xEvent,
            }
          }
          return {
            title: x.name + ((x.createdFor?.id !== uid) ? (" - " + x.createdFor.name) : ""),
            start: activityStart,
            end: activityEnd,
            resizable: { beforeStart: false, afterEnd: false },
            draggable: false,
            color: Calendarcolors["gray"],
            meta: xEvent,
          }

        }, this)
        this.events = [...this.events, ...calData]
        this.filterPlannedEvents()
      },
      error: (e) => {
        this.notifService.openSnackBarWithAction("Error loading planned activities", "Close")
        console.error(e)
      },
    })

    this.constraintService.getConstraints("daily", startTime, endTime).subscribe({
      next: (next) => {
        for (const constraint of next) {
          const event = this.constraintToEvent(constraint)
          if (constraint.isBlacklist) {
            this.events.push(event)
          }
        }
        this.events = [...this.events]
      },
      error: (error) => {
        this.notifService.openSnackBar(error.error?.message)
      },
    })
  }

  private constraintToEvent(constraint: TimeConstraint): CalendarEvent {
    const start = new Date(this.dateParser.parseNumbersIntoDate(constraint.startTime as number[]))
    const end = new Date(this.dateParser.parseNumbersIntoDate(constraint.endTime as number[]))
    return {
      start: start,
      end: end,
      title: constraint.title,
      color: constraint.isBlacklist ? Calendarcolors["dark_gray"] : Calendarcolors["yellow"],
    }
  }

  private filterPlannedEvents() {
    const planned: number[] = this.events.filter((a) => a.meta?.objectType === "finished" &&
      (a.meta.plannedActivity !== undefined)).map((a) => {
      if (a.meta?.plannedActivity?.name) a.title += " - " + a.meta?.plannedActivity?.name;
      return a.meta?.plannedActivity?.id
    });
    this.events = [...this.events.filter((event) => {
      for (const i of planned) {
        if (i === event.meta?.id) {
          return false;
        }
      }
      return true;
    })];
  }
}
