import { NgModule } from '@angular/core';
import { CalendarComponent } from "../component/calendar.component";
import { RouterModule, Routes } from "@angular/router";

const routes: Routes = [
  { path: '', component: CalendarComponent },
];

@NgModule({
  declarations: [CalendarComponent],
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class CalendarModule { }
