import { Component, OnInit } from '@angular/core';
import { TimeConstraintService } from '../service/time-constraints.service';
import { CalendarEvent } from 'angular-calendar';
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
  refresh = new Subject<void>();

  ngOnInit(): void {
    let eventList: CalendarEvent[] = []
    this.constraintService.getConstraints("daily", (new Date()).toLocaleString()).subscribe(
      next => {
        for (let constraint of next) {
          let start = this.parseDate(constraint.startTime)
          let end = this.parseDate(constraint.endTime)
          let event: CalendarEvent = {start: start, end: end, title: constraint.title,
                                       //TODO define Colors
                                       color: {"primary": "#e3bc08", "secondary": "#FDF1BA"} }
          eventList.push(event)
        }
      this.events = eventList
      console.log(this.events)
    })
  }

  parseDate(time: number[]): Date {
    let dateString = `${time[0]}-${time[1]}-${time[2]} ${time[3].toString().padStart(2, '0')}:${time[4].toString().padStart(2, '0')}`
    return new Date(dateString)
  }
}
