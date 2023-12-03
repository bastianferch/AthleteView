import { ChangeDetectorRef, Component } from '@angular/core';
import { ActivityService } from '../../activity/service/activity.service';
import { AuthService } from '../../auth/service/auth.service';
import { CalendarEvent, CalendarView } from 'angular-calendar';
import { EventColor } from "calendar-utils"
import { Activity } from '../../activity/dto/Activity'
import { SnackbarService } from 'src/app/common/service/snackbar.service';
import { add, addMonths, addWeeks, isSameDay, isSameMonth, subMonths, subWeeks, format, startOfWeek, endOfWeek, startOfMonth, endOfMonth } from 'date-fns'
import { enUS } from 'date-fns/locale'
import { Router } from '@angular/router';

@Component({
  selector: 'app-custom-calendar',
  templateUrl: './custom-calendar.component.html',
  styleUrls: ['./custom-calendar.component.scss'],
})
export class CustomCalendarComponent {
  viewDate: Date = new Date()
  viewType: CalendarView = CalendarView.Week
  // helper to check which view is open atm
  CalendarView = CalendarView
  cropStartHour = 5
  cropEndHour = 22
  events: CalendarEvent[] = []
  activeDayIsOpen = false

  // idk about this, suppose we should be using templating colors instead
  colors: Record<string, EventColor> = {
    red: {
      primary: '#ad2121',
      secondary: '#FAE3E3',
    },
    blue: {
      primary: '#1e90ff',
      secondary: '#D1E8FF',
    },
    yellow: {
      primary: '#e3bc08',
      secondary: '#FDF1BA',
    },
  }

  constructor(
    private activityService: ActivityService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
    private notifService: SnackbarService,
    private router: Router,
  ) { }

  ngOnInit() {
    this.loadData()
  }

  handleViewSwitch(view: string) {
    if (view === 'week') {
      // don't need to fetch data
      this.viewType = CalendarView.Week
    } else if (view === 'month') {
      this.viewType = CalendarView.Month
      this.loadData()
    }
    this.cdr.detectChanges()
  }

  handleEventClicked(ev: any) {
    if (ev.title.includes("Planned")) {
      this.router.navigateByUrl(`/activity/${ev.meta.id}`)
    } else {
      // TODO: implement redirection to Activity view once implemented
      this.notifService.openSnackBar("Activity is not planned, thus cannot be opened...")
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
    } else {
      if (past) {
        this.viewDate = subWeeks(this.viewDate, 1)
      } else {
        this.viewDate = addWeeks(this.viewDate, 1)
      }
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

  private parseDate(numbers: number[]): any {
    const str: string[] = []
    numbers.forEach((num) => {
      if (num.toString().length === 1) {
        str.push("0" + num.toString())
      } else {
        str.push(num.toString())
      }
    });
    return str[0] + "-" + str[1] + "-" + str[2] + "T" + str[3] + ":" + str[4]
  }

  private loadData() {
    this.events = []

    // load initial calendar-events
    const uid = this.authService.currentUser.id
    const dateFormatString = "dd.MM.yyyy'T'HH:mm'Z'xxx"

    let startTime = ""
    let endTime = ""
    if (this.viewType === CalendarView.Week) {
      startTime = format(startOfWeek(this.viewDate), dateFormatString)
      endTime = format(endOfWeek(this.viewDate), dateFormatString)
    } else {
      startTime = format(startOfMonth(this.viewDate), dateFormatString)
      endTime = format(endOfMonth(this.viewDate), dateFormatString)
    }

    this.activityService.fetchAllActivitiesForUser(uid, startTime, endTime).subscribe({
      next: (data: Array<Activity>) => {
        const calData = data.map((x) => {
          return {
            title: "Finished activity",
            start: new Date(this.parseDate(x.startTime)),
            end: new Date(this.parseDate(x.endTime)),
            resizable: { beforeStart: false, afterEnd: false },
            draggable: false,
            color: { ...this.colors["blue"] },
            meta: x,
          }
        }, this)
        this.events = [...this.events, ...calData]
      },
      error: (e) => {
        this.notifService.openSnackBarWithAction("Error trying to fetch completed activities", "X")
        console.error(e)
      },
    })

    this.activityService.fetchAllPlannedActivitiesForUser(uid, startTime, endTime).subscribe({
      next: (data) => {
        const calData: CalendarEvent[] = data.map((x) => {
          const activityStart = new Date(this.parseDate(x.date as number[]))
          const activityEnd = add(activityStart, { minutes: 120 })

          return {
            title: "Planned Activity",
            start: activityStart,
            end: activityEnd,
            resizable: { beforeStart: false, afterEnd: false },
            draggable: false,
            color: { ...this.colors["red"] },
            meta: x,
          }
        }, this)
        this.events = [...this.events, ...calData]
      },
      error: (e) => {
        this.notifService.openSnackBarWithAction("Error loading planned activities", "X")
        console.error(e)
      },
    })
  }
}
