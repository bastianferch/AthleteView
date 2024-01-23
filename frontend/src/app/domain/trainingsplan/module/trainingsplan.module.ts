import { NgModule } from '@angular/core';
import { RouterModule, Routes } from "@angular/router";
import { TrainingsplanComponent } from "../component/trainingsplan.component";
import { AsyncPipe, NgForOf, NgIf } from "@angular/common";
import { CdkDrag, CdkDropList } from "@angular/cdk/drag-drop";
import { MatCardModule } from "@angular/material/card";
import { MatIconModule } from "@angular/material/icon";
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from "@angular/material/button";
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from "@angular/material/form-field";
import {ModalComponent} from "../component/modal/modal.component";


const routes: Routes = [
  { path: '', component: TrainingsplanComponent },
]
@NgModule({
  declarations: [TrainingsplanComponent],
  imports: [RouterModule.forChild(routes), NgForOf, CdkDrag, CdkDropList, FormsModule, MatFormFieldModule, MatCardModule, MatIconModule, MatCheckboxModule, MatButtonModule, ReactiveFormsModule, NgIf, AsyncPipe],
  exports: [RouterModule],
})
export class TrainingsplanModule { }
