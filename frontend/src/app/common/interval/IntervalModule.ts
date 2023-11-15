import { RouterModule, Routes } from "@angular/router";
import { NgModule } from "@angular/core";
import { IntervalContainerComponent } from "./component/interval-container/interval-container.component";
import { IntervalComponent } from "./component/interval/interval.component";
import { MatButtonModule } from "@angular/material/button";
import { MatCardModule } from "@angular/material/card";
import { NgForOf, NgIf, NgStyle, NgTemplateOutlet } from "@angular/common";
import { StepComponent } from "./component/step/step.component";
import { CdkDrag, CdkDragHandle, CdkDragPlaceholder, CdkDropList } from "@angular/cdk/drag-drop";
import { MatDialogModule } from "@angular/material/dialog";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatIconModule } from "@angular/material/icon";

const routes: Routes = [
  { path: '', component: IntervalContainerComponent },
];

// TODO this module is only here for testing the interval component. remove later.
@NgModule({
  declarations: [IntervalContainerComponent, IntervalComponent, StepComponent],
  imports: [MatDialogModule, RouterModule.forChild(routes), MatButtonModule, MatCardModule, NgForOf, NgIf, NgTemplateOutlet, CdkDropList, CdkDrag, CdkDragPlaceholder, MatFormFieldModule, MatIconModule, NgStyle, CdkDragHandle],
  exports: [RouterModule, IntervalContainerComponent],
})
export class IntervalModule {}
