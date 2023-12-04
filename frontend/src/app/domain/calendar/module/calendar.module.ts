import { NgModule } from '@angular/core';
import { CalendarComponent } from "../component/calendar.component";
import { MatButtonModule } from "@angular/material/button";
import { RouterModule, Routes } from "@angular/router";
import { CalendarModule, DateAdapter } from 'angular-calendar';
import { adapterFactory } from 'angular-calendar/date-adapters/date-fns';

const routes: Routes = [
  { path: '', component: CalendarComponent },
];

@NgModule({
  declarations: [CalendarComponent],
  imports: [
    RouterModule.forChild(routes),
    CalendarModule.forRoot({
      provide: DateAdapter,
      useFactory: adapterFactory,
    }),
    MatButtonModule,
  ],
  exports: [RouterModule],
})
export class CalendarViewModule { }
