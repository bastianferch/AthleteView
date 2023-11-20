import { Component, EventEmitter, Input, Output } from '@angular/core';
import {
  Step,
  StepDurationDistanceUnitMapper,
  StepDurationMapper,
  StepDurationType,
  StepNameMapper, StepTargetMapper, StepType,
} from "../../dto/Step";
import { EditStepDialogComponent } from "../edit-step-dialog/edit-step-dialog.component";
import { MatDialog } from "@angular/material/dialog"
import { IntervalService } from "../../service/interval.service";
import { ActivityNameMapper, ActivityType } from "../../../dto/PlannedActivity";
@Component({
  selector: 'app-step',
  templateUrl: './step.component.html',
  styleUrls: ['./step.component.scss', '../../style/interval.scss'],
})
export class StepComponent {
  @Input() step: Step;
  @Input() editable: boolean;
  @Input() activityType: ActivityType;
  @Output() deleteStep: EventEmitter<any> = new EventEmitter();
  @Output() changeStep: EventEmitter<Step> = new EventEmitter();

  // so the html can see the type
  protected readonly StepDurationType = StepDurationType;
  protected activityMapper;
  protected stepNameMapper;
  protected stepDurationMapper;
  protected stepDurationDistanceUnitMapper;
  protected stepTargetMapper;
  constructor(public dialog: MatDialog, protected service: IntervalService) {
    this.activityMapper = ActivityNameMapper;
    this.stepNameMapper = StepNameMapper;
    this.stepDurationMapper = StepDurationMapper;
    this.stepDurationDistanceUnitMapper = StepDurationDistanceUnitMapper;
    this.stepTargetMapper = StepTargetMapper;
  }

  openDialog(): void {
    const dialogRef = this.dialog.open(EditStepDialogComponent, {
      data: [this.step, this.activityType],
    });

    dialogRef.afterClosed().subscribe((result: Step) => {
      if (result !== undefined) {
        this.changeStep.emit(result);
      }
    });
  }

  onDeleteStep() {
    this.deleteStep.emit();
  }

  onEditStep() {
    this.openDialog();
  }

  protected readonly StepType = StepType;
}
