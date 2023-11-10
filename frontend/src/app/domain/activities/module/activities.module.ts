import { NgModule } from '@angular/core';
import { ActivitiesComponent } from "../component/activities.component";
import { RouterModule, Routes } from "@angular/router";

const routes: Routes = [
  { path: '', component: ActivitiesComponent },
];
@NgModule({
  declarations: [ActivitiesComponent],
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ActivitiesModule { }
