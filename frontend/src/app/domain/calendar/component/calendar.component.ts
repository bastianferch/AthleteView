import { Component, OnInit } from '@angular/core';
import { TimeConstraintService } from '../../time-constraints/service/time-constraints.service';
import { TimeConstraint } from '../../../common/dto/TimeConstraint';
import { CalendarEvent } from 'angular-calendar';
import { Calendarcolors } from "../../../common/util/calendar-colors";


@Component({
  selector: 'app-calendar',
  templateUrl: './calendar.component.html',
  styleUrls: ['./calendar.component.scss'],
})
export class CalendarComponent implements OnInit {

  viewDate: Date = new Date()
  events: CalendarEvent[] = []
  firstOfMonth: Date
  lastOfMonth: Date

  constructor(private constraintService: TimeConstraintService) {}

  ngOnInit() {
    const date = new Date();
    this.firstOfMonth = new Date(date.getFullYear(), date.getMonth(), 1);
    this.lastOfMonth = new Date(date.getFullYear(), date.getMonth() + 1, 0);
    this.getConstraints()
  }

  getConstraints() {
    this.constraintService.getConstraints("daily", this.firstOfMonth.toLocaleString(), this.lastOfMonth.toLocaleString()).subscribe(
      (next) => {
        for (const constraint of next) {
          const event = this.constraintToEvent(constraint)
          if (constraint.isBlacklist) {
            this.events.push(event)
          }
        }
        this.events = [...this.events]
      })
  }

  // for some reason the date is returned as a number[] from the backend, and typescript just cannot deal with this
  parseDate(time: any): Date {
    const dateString = `${time[0]}-${time[1]}-${time[2]} ${time[3].toString().padStart(2, '0')}:${time[4].toString().padStart(2, '0')}`
    return new Date(dateString)
  }

  constraintToEvent(constraint: TimeConstraint): CalendarEvent {
    const start = this.parseDate(constraint.startTime)
    const end = this.parseDate(constraint.endTime)
    const event: CalendarEvent = { start: start, end: end, title: constraint.title, color: constraint.isBlacklist ? Calendarcolors["green"] : Calendarcolors["yellow"] }
    return event
  }
}
