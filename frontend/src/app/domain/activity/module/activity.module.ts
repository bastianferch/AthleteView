import { NgModule } from '@angular/core';
import { ActivityComponent } from '../component/activity.component';
import { RouterModule, Routes } from '@angular/router';
import {
  ActivityCreateEditViewMode,
  CreateEditViewActivityComponent,
} from '../component/create/create-edit-view-activity.component';
import { IntervalModule } from '../../../common/interval/IntervalModule';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { NgForOf, NgIf } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatIconModule } from "@angular/material/icon";


const routes: Routes = [
  { path: '', component: ActivityComponent },
  { path: 'create', component: CreateEditViewActivityComponent, data: { mode: ActivityCreateEditViewMode.create } },
  { path: ':id', component: CreateEditViewActivityComponent, data: { mode: ActivityCreateEditViewMode.details } }, // TODO when navigating to non-existent activity, a default one is shown. ERROR PAGE! And why can I edit stuff in the view component?
  { path: ':id/edit', component: CreateEditViewActivityComponent, data: { mode: ActivityCreateEditViewMode.edit } },
];

@NgModule({
  declarations: [ActivityComponent, CreateEditViewActivityComponent],
  imports: [RouterModule.forChild(routes), IntervalModule, MatFormFieldModule, MatSelectModule, NgForOf, FormsModule, MatInputModule, MatCheckboxModule, MatButtonModule, MatDatepickerModule, MatNativeDateModule, NgIf, ReactiveFormsModule, MatIconModule],
  exports: [RouterModule],
})
export class ActivityModule {
}
