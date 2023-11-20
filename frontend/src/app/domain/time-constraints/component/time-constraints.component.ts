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
  blacklist: CalendarEvent[] = []
  whitelist: CalendarEvent[] = []
  // show whitelist constraints | show blacklist constraints
  show: boolean[] = [false, true]
  refresh = new Subject<void>();
  //TODO import from some global color scheme
  colors = [
    {primary: '#e3bc08', secondary: '#FDF1BA'},
    {primary: '#1e90ff', secondary: '#D1E8FF'}
  ]

  ngOnInit(): void {
    this.getEvents()
  }

  getEvents() {
      let eventList: CalendarEvent[] = []
      this.constraintService.getConstraints("daily", (new Date()).toLocaleString()).subscribe(
        next => {
          for (let constraint of next) {
            if (constraint.isBlacklist)
              this.blacklist.push(this.constraintToEvent(constraint))
            else
              this.whitelist.push(this.constraintToEvent(constraint))
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

  parseDate(time: number[]): Date {
    let dateString = `${time[0]}-${time[1]}-${time[2]} ${time[3].toString().padStart(2, '0')}:${time[4].toString().padStart(2, '0')}`
    return new Date(dateString)
  }

  constraintToEvent(constraint: any): CalendarEvent {
    let start = this.parseDate(constraint.startTime)
    let end = this.parseDate(constraint.endTime)
    let event: CalendarEvent = {start: start, end: end, title: constraint.title, color: constraint.isBlacklist ? this.colors[0] : this.colors[1] }
    return event
  }
}
