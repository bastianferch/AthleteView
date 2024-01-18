import { NgModule } from '@angular/core';
import { HomeComponent } from '../component/home.component';
import { RouterModule, Routes } from "@angular/router";
import { NgForOf, NgIf } from "@angular/common";
import { CustomCalendarComponent } from "../../calendar/component/custom-calendar.component";
import { CalendarModule, DateAdapter } from "angular-calendar";
import { adapterFactory } from "angular-calendar/date-adapters/date-fns";
import { MatCardModule } from "@angular/material/card";
import { MatIconModule } from "@angular/material/icon";
import { MatButtonModule } from "@angular/material/button";

const routes: Routes = [
  { path: '', component: HomeComponent },
];

@NgModule({
  declarations: [
    HomeComponent,
  ],
  imports: [
    RouterModule.forChild(routes),
    NgForOf,
    NgIf,
    CalendarModule.forRoot({ provide: DateAdapter, useFactory: adapterFactory }),
    CustomCalendarComponent,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
  ],
  exports: [RouterModule],
})
export class HomeModule { }
