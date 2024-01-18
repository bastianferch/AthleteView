import { NgModule } from '@angular/core';
import { CustomCalendarComponent } from "../component/custom-calendar.component";
import { RouterModule, Routes } from "@angular/router";
import { CalendarModule, DateAdapter } from 'angular-calendar';
import { adapterFactory } from 'angular-calendar/date-adapters/date-fns';


const routes: Routes = [
  { path: '', component: CustomCalendarComponent },
];

@NgModule({
  declarations: [],
  imports: [
    RouterModule.forChild(routes),
    CalendarModule.forRoot({ provide: DateAdapter, useFactory: adapterFactory }),
    CustomCalendarComponent,
  ],
  exports: [RouterModule],
})
export class CustomCalendarModule { }
