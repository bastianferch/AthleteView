import { Component } from '@angular/core';
import { CalendarEvent } from 'angular-calendar';


@Component({
  selector: 'app-calendar',
  templateUrl: './calendar.component.html',
  styleUrls: ['./calendar.component.scss'],
})
export class CalendarComponent {

  viewDate: Date = new Date()
  events: CalendarEvent[] = []

}
