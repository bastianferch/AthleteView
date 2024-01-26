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
import { CommonModule, NgForOf, NgIf } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatIconModule } from "@angular/material/icon";
import { MatListModule } from '@angular/material/list';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatTableModule } from '@angular/material/table';
import { FinishedActivityComponent } from '../component/finished-activity/finished-activity.component';
import { MatGridListModule } from "@angular/material/grid-list";
import { MatTabsModule } from '@angular/material/tabs';
import { MatDividerModule } from '@angular/material/divider';
import { MatPaginatorModule } from '@angular/material/paginator';
import { CommentsComponent } from '../component/comments/comments.component'
import { RatingComponent } from '../component/rating/rating.component';
import { MatCardModule } from "@angular/material/card";

import { LeafletModule } from '@asymmetrik/ngx-leaflet';
import { HighchartsChartModule } from 'highcharts-angular';
import { ActivityGraphComponent } from '../component/finished-activity/activity-graph/activity-graph.component';
import { MatTooltipModule } from '@angular/material/tooltip';

const routes: Routes = [
  { path: '', component: ActivityComponent },
  { path: 'create', component: CreateEditViewActivityComponent, data: { mode: ActivityCreateEditViewMode.create } },
  { path: ':id', component: CreateEditViewActivityComponent, data: { mode: ActivityCreateEditViewMode.details } },
  { path: 'finished/:id', component: FinishedActivityComponent },
  { path: ':id/edit', component: CreateEditViewActivityComponent, data: { mode: ActivityCreateEditViewMode.edit } },
];

@NgModule({
  declarations: [
    ActivityComponent,
    CreateEditViewActivityComponent,
    FinishedActivityComponent,
    CommentsComponent,
    RatingComponent,
    ActivityGraphComponent,
  ],
  imports: [
    RouterModule.forChild(routes),
    IntervalModule,
    MatFormFieldModule,
    MatSelectModule,
    NgForOf,
    FormsModule,
    MatInputModule,
    MatCheckboxModule,
    MatButtonModule,
    MatDatepickerModule,
    MatNativeDateModule,
    NgIf,
    ReactiveFormsModule,
    MatIconModule,
    MatListModule,
    MatButtonToggleModule,
    CommonModule,
    MatTableModule,
    MatGridListModule,
    MatTabsModule,
    MatDividerModule,
    MatPaginatorModule,
    LeafletModule,
    HighchartsChartModule,
    MatTooltipModule,
    MatCardModule,
  ],
  exports: [RouterModule],
})
export class ActivityModule {
}
