import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CreateTimeConstraintsComponent } from '../component/create/create-time-constraints.component';
import { TimeConstraintsComponent } from '../component/time-constraints.component';
import { RouterModule, Routes } from "@angular/router";
import { MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { NgxMaterialTimepickerModule } from 'ngx-material-timepicker';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { NgIf } from '@angular/common';
import { MatChipsModule } from '@angular/material/chips';
import { MatButtonModule } from '@angular/material/button';
import { CalendarModule, DateAdapter } from 'angular-calendar';
import { MatDialogModule } from "@angular/material/dialog";
import { adapterFactory } from 'angular-calendar/date-adapters/date-fns';
import { MatDividerModule } from '@angular/material/divider';


const routes: Routes = [
  { path: '', component: TimeConstraintsComponent },
];

@NgModule({
  declarations: [
    TimeConstraintsComponent,
    CreateTimeConstraintsComponent,
  ],
  imports: [
    RouterModule.forChild(routes),
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatIconModule,
    NgxMaterialTimepickerModule,
    MatButtonToggleModule,
    NgIf,
    MatChipsModule,
    MatButtonModule,
    CalendarModule.forRoot({
      provide: DateAdapter,
      useFactory: adapterFactory,
    }),
    FormsModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatDividerModule,
  ],
  exports: [RouterModule],
})
export class TimeConstraintsModule { }
