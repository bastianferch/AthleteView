import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from "@angular/material/dialog";
import { Step, StepDurationDistanceUnit, StepDurationType, StepTargetType, StepType } from "../../dto/Step";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { FormsModule } from "@angular/forms";
import { MatButtonModule } from "@angular/material/button";
import { MatSelectModule } from "@angular/material/select";
import { NgForOf, NgIf } from "@angular/common";
import { MatDividerModule } from "@angular/material/divider";
import { IntervalService } from "../../service/interval.service";

@Component({
  selector: 'app-edit-step-dialog',
  templateUrl: './edit-step-dialog.component.html',
  styleUrls: ['./edit-step-dialog.component.scss'],
  imports: [MatDialogModule, MatFormFieldModule, MatInputModule, FormsModule, MatButtonModule, MatSelectModule, NgForOf, NgIf, MatDividerModule],
  standalone: true,
})
export class EditStepDialogComponent implements OnInit {

  stepCopy: Step;

  public stepTypes = Object.values(StepType);
  public durationTypes = Object.values(StepDurationType);
  public distanceUnits = Object.values(StepDurationDistanceUnit);
  public targetTypes = Object.values(StepTargetType);

  constructor(
    public dialogRef: MatDialogRef<EditStepDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Step,
    protected service: IntervalService,
  ) {}

  // copy the object aso the original object is not mutated
  ngOnInit(): void {
    this.stepCopy = Object.assign({}, this.data);
  }

  // closes the dialog with result == undefined, so no changes are performed
  onNoClick(): void {
    this.dialogRef.close(this.data);
  }

  prepareResult() {
    if (!this.showDurationSettings()) {
      this.stepCopy.duration_distance = undefined;
      this.stepCopy.duration_distance_unit = undefined;
    }

    if (!this.showIntensitySettings()) {
      this.stepCopy.target_from = undefined;
      this.stepCopy.target_to = undefined;
    }
  }

  showDurationSettings() {
    return !(this.stepCopy.duration_type === StepDurationType.LAPBUTTON) && !(this.stepCopy.duration_type === undefined);
  }

  showIntensitySettings() {
    return !(this.stepCopy.target_type === undefined);
  }

}
