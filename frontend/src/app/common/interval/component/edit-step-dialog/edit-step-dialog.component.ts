import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from "@angular/material/dialog";
import {
  Step,
  StepDurationMapper,
  StepDurationType,
  StepDurationUnit,
  StepDurationUnitMapper,
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
  public distanceUnits = Object.values(StepDurationUnit);
  public targetTypes = Object.values(StepTargetType);

  protected readonly StepType = StepType;
  protected readonly StepTargetType = StepTargetType;

  protected activityMapper;
  protected stepNameMapper;
  protected stepDurationMapper;
  protected stepDurationUnitMapper;
  protected stepTargetMapper;


  form = new FormGroup({
    type: new FormControl(null, [Validators.required]),
    durationType: new FormControl(null, [Validators.required]),
    duration: new FormControl(null),
    durationUnit: new FormControl(null),
    targetType: new FormControl(null),
    targetFrom: new FormControl(null),
    targetTo: new FormControl(null),
    targetFrom2: new FormControl(null),
    targetTo2: new FormControl(null),
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
    this.stepDurationUnitMapper = StepDurationUnitMapper;
    this.stepTargetMapper = StepTargetMapper;
  }

  // copy the object aso the original object is not mutated
  ngOnInit(): void {
    this.stepCopy = Object.assign({}, this.data[0]);
    this.form.patchValue({
      type: this.stepCopy.type,
      durationType: this.stepCopy.durationType,
      duration: this.stepCopy.duration,
      durationUnit: this.stepCopy.durationUnit,
      targetType: this.stepCopy.targetType,
      targetFrom: this.stepCopy.targetFrom,
      targetFrom2: this.service.convertToMinSec(this.stepCopy.targetFrom),
      targetTo: this.stepCopy.targetTo,
      targetTo2: this.service.convertToMinSec(this.stepCopy.targetTo),
      note: this.stepCopy.note,
    });

    if (this.stepCopy.targetType === null) {
      this.form.patchValue({
        targetType: undefined,
      });
    }

    this.updateFormValidators()

    // set conditional validators
    this.form.get('durationType').valueChanges.subscribe(() => {
      this.updateFormValidators()

      // if the current unit type is not compatible with the duration type, change it
      const validTypes = this.getDurationOptionsForType()
      if (!(validTypes.includes(this.form.value.durationUnit))) {
        this.form.patchValue({
          durationUnit: validTypes[0],
        });
      }
    });
    this.form.get('targetType').valueChanges.subscribe(() => {
      this.updateFormValidators()

    });
  }

  // closes the dialog with result == undefined, so no changes are performed
  onNoClick(): void {
    this.dialogRef.close(this.data[0]);
  }

  prepareResult() {
    // if duration settings are not visible (intensity target type is "press lap button"), remove values from form
    if (!this.showDurationSettings()) {
      this.form.patchValue({
        duration: undefined,
        durationUnit: undefined,
      });
    }

    // if intensity settings are not visible (intensity target type is "none"), remove values from form
    if (!this.showIntensitySettings()) {
      this.form.patchValue({
        targetFrom: undefined,
        targetFrom2: undefined,
        targetTo: undefined,
        targetTo2: undefined,
      });
    }

    // return the form values as result of this dialog
    this.dialogRef.close(this.form.value)
  }

  showDurationSettings() {
    return (
      !(this.form.get('durationType').value === StepDurationType.LAPBUTTON)
      && !(this.form.get('durationType').value === undefined)
    );
  }

  showIntensitySettings() {
    return !(this.form.get('targetType').value === undefined);
  }

  getDurationOptionsForType() {
    if (this.form.value.durationType === StepDurationType.DISTANCE) {
      return [StepDurationUnit.KM, StepDurationUnit.M]
    }

    if (this.form.value.durationType === StepDurationType.TIME) {
      return [StepDurationUnit.MIN, StepDurationUnit.SEC]
    }

    return []
  }


  private updateFormValidators() {
    // if durationType !== "Press Lap Button", duration and durationUnit are required
    if (this.showDurationSettings()) {
      this.form.controls['duration'].setValidators([Validators.required]);
      this.form.controls['durationUnit'].setValidators([Validators.required]);
    } else {
      this.form.controls['duration'].clearValidators();
      this.form.controls['durationUnit'].clearValidators();
    }
    this.form.controls['duration'].updateValueAndValidity();
    this.form.controls['durationUnit'].updateValueAndValidity();

    // if targetType !== "none", targetFrom and targetTo are required
    if (this.showIntensitySettings()) {
      this.form.controls['targetFrom'].setValidators([Validators.required]);
      this.form.controls['targetTo'].setValidators([Validators.required]);
    } else {
      this.form.controls['targetFrom'].clearValidators();
      this.form.controls['targetTo'].clearValidators();
    }
    this.form.controls['targetFrom'].updateValueAndValidity();
    this.form.controls['targetTo'].updateValueAndValidity();
  }
}
