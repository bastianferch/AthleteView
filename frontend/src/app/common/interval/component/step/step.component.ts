import { Component, EventEmitter, Input, Output } from '@angular/core';
import {
  Step,
  StepDurationDistanceUnitMapper,
  StepDurationMapper,
  StepDurationType,
  StepNameMapper,
  StepTargetMapper,
  StepTargetType,
  StepType,
} from "../../dto/Step";
import { EditStepDialogComponent } from "../edit-step-dialog/edit-step-dialog.component";
import { MatDialog } from "@angular/material/dialog"
import { IntervalService } from "../../service/interval.service";
import { ActivityNameMapper, ActivityType } from "../../../../domain/activity/dto/PlannedActivity";

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

  protected readonly StepType = StepType;
  // so the html can see the type
  protected readonly StepDurationType = StepDurationType;
  protected readonly StepTargetType = StepTargetType;

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

    dialogRef.afterClosed().subscribe((result) => {
      if (result !== undefined) {
        if (result.targetType === StepTargetType.PACE) {

          result.targetFrom = this.service.convertToSeconds(result['targetFrom2']);
          result.targetTo = this.service.convertToSeconds(result['targetTo2']);
        }
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
}
