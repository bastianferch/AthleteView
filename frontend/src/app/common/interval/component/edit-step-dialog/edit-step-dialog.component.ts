import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from "@angular/material/dialog";
import {
  Step,
  StepDurationDistanceUnit,
  StepDurationDistanceUnitMapper,
  StepDurationMapper,
  StepDurationType,
  StepNameMapper,
  StepTargetMapper,
  StepTargetType,
  StepType,
} from "../../dto/Step";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from "@angular/forms";
import { MatButtonModule } from "@angular/material/button";
import { MatSelectModule } from "@angular/material/select";
import { NgForOf, NgIf } from "@angular/common";
import { MatDividerModule } from "@angular/material/divider";
import { IntervalService } from "../../service/interval.service";
import { ActivityNameMapper, ActivityType } from "../../../../domain/activity/dto/PlannedActivity";
import { MatSliderModule } from "@angular/material/slider";

@Component({
  selector: 'app-edit-step-dialog',
  templateUrl: './edit-step-dialog.component.html',
  styleUrls: ['./edit-step-dialog.component.scss'],
  imports: [MatDialogModule, MatFormFieldModule, MatInputModule, FormsModule, MatButtonModule, MatSelectModule, NgForOf, NgIf, MatDividerModule, MatSliderModule, ReactiveFormsModule],
  standalone: true,
})
export class EditStepDialogComponent implements OnInit {
  stepCopy: Step;

  public stepTypes = Object.values(StepType);
  public durationTypes = Object.values(StepDurationType);
  public distanceUnits = Object.values(StepDurationDistanceUnit);
  public targetTypes = Object.values(StepTargetType);

  protected readonly StepType = StepType;
  protected readonly StepTargetType = StepTargetType;

  protected activityMapper;
  protected stepNameMapper;
  protected stepDurationMapper;
  protected stepDurationDistanceUnitMapper;
  protected stepTargetMapper;


  form = new FormGroup({
    type: new FormControl(null, [Validators.required]), // TODO add additional validators like time format
    durationType: new FormControl(null, [Validators.required]),
    durationDistance: new FormControl(null, [Validators.required]),
    durationDistanceUnit: new FormControl(null, [Validators.required]),
    targetType: new FormControl(null),
    targetFrom: new FormControl(null, [Validators.required]),
    targetTo: new FormControl(null, [Validators.required]),
    targetFrom2: new FormControl(null, [Validators.required]),
    targetTo2: new FormControl(null, [Validators.required]),
    note: new FormControl(null),
  });


  constructor(
    public dialogRef: MatDialogRef<EditStepDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: [Step, ActivityType],
    protected service: IntervalService,
  ) {
    this.activityMapper = ActivityNameMapper;
    this.stepNameMapper = StepNameMapper;
    this.stepDurationMapper = StepDurationMapper;
    this.stepDurationDistanceUnitMapper = StepDurationDistanceUnitMapper;
    this.stepTargetMapper = StepTargetMapper;
  }

  // copy the object aso the original object is not mutated
  ngOnInit(): void {
    this.stepCopy = Object.assign({}, this.data[0]);
    this.form.patchValue({
      type: this.stepCopy.type,
      durationType: this.stepCopy.durationType,
      durationDistance: this.stepCopy.duration,
      durationDistanceUnit: this.stepCopy.durationUnit,
      targetType: this.stepCopy.targetType,
      targetFrom: this.stepCopy.targetFrom,
      targetFrom2: this.service.convertToMinSec(this.stepCopy.targetFrom),
      targetTo: this.stepCopy.targetTo,
      targetTo2: this.service.convertToMinSec(this.stepCopy.targetTo),
      note: this.stepCopy.note,
    });
  }

  // closes the dialog with result == undefined, so no changes are performed
  onNoClick(): void {
    this.dialogRef.close(this.data[0]);
  }

  prepareResult() {
    if (!this.showDurationSettings()) {
      this.stepCopy.duration = undefined;
      this.stepCopy.durationUnit = undefined;
    }

    if (!this.showIntensitySettings()) {
      this.stepCopy.targetFrom = undefined;
      this.stepCopy.targetTo = undefined;
    }
  }

  showDurationSettings() {
    return !(this.form.get('durationType').value === StepDurationType.LAPBUTTON) && !(this.form.get('durationType').value === undefined);
  }

  showIntensitySettings() {
    return !(this.form.get('targetType').value === undefined);
  }
}
