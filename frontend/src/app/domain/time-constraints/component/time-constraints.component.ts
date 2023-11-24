import { Component, OnInit } from '@angular/core';
import { TimeConstraintService } from '../service/time-constraints.service';
import { TimeConstraint } from '../../../common/dto/TimeConstraint';
import { CalendarEvent, CalendarEventAction, CalendarView, CalendarEventTimesChangedEvent } from 'angular-calendar';
import { Subject } from 'rxjs';

@Component({
  selector: 'app-time-constraints',
  templateUrl: './time-constraints.component.html',
  styleUrls: ['./time-constraints.component.scss'],
})
export class TimeConstraintsComponent implements OnInit {

  constructor(private constraintService: TimeConstraintService) {}

  viewDate: Date = new Date()
  events: CalendarEvent[] = []
  blacklist: CalendarEvent[] = []
  whitelist: CalendarEvent[] = []
  eventMap: Map<CalendarEvent, number>
  // show whitelist constraints | show blacklist constraints
  show: boolean[] = [false, true]
  startOfWeek: Date
  refresh = new Subject<void>();
  //TODO import from some global color scheme
  colors = [
    {primary: '#e3bc08', secondary: '#FDF1BA'},
    {primary: '#1e90ff', secondary: '#D1E8FF'}
  ]

  actions: CalendarEventAction[] = [
      {
        label: 'X',
        a11yLabel: 'Delete',
        onClick: ({ event }: { event: CalendarEvent }): void => {

          this.constraintService.delete(this.eventMap.get(event)).subscribe(
            next => this.getEvents()
          );
        },
      },
    ];

  ngOnInit(): void {
    this.setStartOfWeek()
    this.getEvents()
  }

  getEvents() {
      let eventList: CalendarEvent[] = []
      this.eventMap = new Map<CalendarEvent, number>()
      this.constraintService.getConstraints("daily", this.startOfWeek.toLocaleString()).subscribe(
        next => {
        this.whitelist = []
        this.blacklist = []
          for (let constraint of next) {
            let event = this.constraintToEvent(constraint)
            if (constraint.isBlacklist)
              this.blacklist.push(event)
            else
              this.whitelist.push(event)
              this.eventMap.set(event, constraint.id)
          }
        this.setEvents()
        console.log(this.events)
      })
  }

  setChoice(choice: string[]) {
    this.show[0] = (choice.indexOf("white") != -1)
    this.show[1] = (choice.indexOf("black") != -1)
    this.setEvents()
  }

  setEvents() {
    this.events = []
    if(this.show[0]) this.events = [...this.whitelist]
    if(this.show[1]) this.events = [...this.events, ...this.blacklist]
  }

  // for some reason the date is returned as a number[] from the backend, and typescript just cannot deal with this
  parseDate(time: any): Date {
    let dateString = `${time[0]}-${time[1]}-${time[2]} ${time[3].toString().padStart(2, '0')}:${time[4].toString().padStart(2, '0')}`
    return new Date(dateString)
  }

  constraintToEvent(constraint: TimeConstraint): CalendarEvent {
    let start = this.parseDate(constraint.startTime)
    let end = this.parseDate(constraint.endTime)
    let event: CalendarEvent = {start: start, end: end, title: constraint.title, color: constraint.isBlacklist ? this.colors[0] : this.colors[1], actions: this.actions }
    return event
  }

  setView(days: number) {
    this.viewDate = new Date(this.viewDate.setDate(this.viewDate.getDate() + days))
    this.setStartOfWeek()
    this.getEvents()
  }

  setStartOfWeek() {
    this.startOfWeek = this.viewDate
    this.startOfWeek.setDate(this.startOfWeek.getDate()-this.startOfWeek.getDay()+1)
    this.startOfWeek.setHours(0,0,0,0)
  }
}
