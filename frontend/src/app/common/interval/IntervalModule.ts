import { RouterModule, Routes } from "@angular/router";
import { NgModule } from "@angular/core";
import { IntervalContainerComponent } from "./component/interval-container/interval-container.component";
import { IntervalComponent } from "./component/interval/interval.component";
import { MatButtonModule } from "@angular/material/button";
import { MatCardModule } from "@angular/material/card";
import { NgForOf, NgIf, NgTemplateOutlet } from "@angular/common";
import { StepComponent } from "./component/step/step.component";
import { CdkDrag, CdkDragPlaceholder, CdkDropList } from "@angular/cdk/drag-drop";

const routes: Routes = [
  { path: '', component: IntervalContainerComponent },
];

// TODO this module is only here for testing the interval component. remove later.
@NgModule({
  declarations: [IntervalContainerComponent, IntervalComponent, StepComponent],
  imports: [RouterModule.forChild(routes), MatButtonModule, MatCardModule, NgForOf, NgIf, NgTemplateOutlet, CdkDropList, CdkDrag, CdkDragPlaceholder],
  exports: [RouterModule, IntervalContainerComponent],
})
export class IntervalModule {}
