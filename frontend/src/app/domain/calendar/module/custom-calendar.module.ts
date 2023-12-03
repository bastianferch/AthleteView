import { NgModule } from '@angular/core';
import { CustomCalendarComponent } from "../component/custom-calendar.component";
import { RouterModule, Routes } from "@angular/router";
import { CalendarModule, DateAdapter } from 'angular-calendar';
import { adapterFactory } from 'angular-calendar/date-adapters/date-fns';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatButtonModule } from '@angular/material/button';


const routes: Routes = [
  { path: '', component: CustomCalendarComponent },
];

@NgModule({
  declarations: [CustomCalendarComponent],
  imports: [
    RouterModule.forChild(routes),
    CalendarModule.forRoot({ provide: DateAdapter, useFactory: adapterFactory }),
    MatButtonToggleModule,
    MatButtonModule,
  ],
  exports: [RouterModule],
})
export class CustomCalendarModule { }
