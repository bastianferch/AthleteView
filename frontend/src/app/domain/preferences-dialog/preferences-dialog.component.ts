import { Component, OnInit } from '@angular/core';
import { UserService } from "../user/service/UserService";
import { PreferenceNotificationMapper, PreferenceNotificationType, PreferencesDto } from "../user/dto/preferences-dto";
import { MatDialogModule, MatDialogRef } from "@angular/material/dialog";
import { StepType } from "../../common/interval/dto/Step";
import { FormControl, FormGroup, ReactiveFormsModule } from "@angular/forms";
import { MatInputModule } from "@angular/material/input";
import { MatSelectModule } from "@angular/material/select";
import { MatSlideToggleModule } from "@angular/material/slide-toggle";
import { NgForOf, NgIf } from "@angular/common";
import { MatButtonModule } from "@angular/material/button";

@Component({
  selector: 'app-preferences-dialog',
  templateUrl: './preferences-dialog.component.html',
  styleUrls: ['./preferences-dialog.component.scss'],
  standalone: true,
  imports: [
    MatInputModule,
    MatSelectModule,
    MatSlideToggleModule,
    ReactiveFormsModule,
    MatDialogModule,
    NgIf,
    MatButtonModule,
    NgForOf,
  ],
})
export class PreferencesDialogComponent implements OnInit {

  preferences: PreferencesDto;

  protected readonly preferenceNotificationType = Object.values(PreferenceNotificationType)
  protected preferenceNotificationMapper = PreferenceNotificationMapper
  protected readonly StepType = StepType;

  form: FormGroup = null;

  constructor(
    public dialogRef: MatDialogRef<PreferencesDialogComponent>,
    private userService: UserService,
  ) {}

  ngOnInit() {
    this.getPreferences()
  }


  getPreferences() {
    this.userService.getPreferences().subscribe((data) => {
      if (data !== null && data !== undefined) {
        this.form = new FormGroup({
          email: new FormControl(data.emailNotifications),
          comment: new FormControl(data.commentNotifications),
          rating: new FormControl(data.ratingNotifications),
          other: new FormControl(data.otherNotifications),
          health: new FormControl(data.shareHealthWithTrainer),
        })
        this.preferences = data;
      }
    })
  }

  patchPreferences() {
    const newPrefs: PreferencesDto = {
      emailNotifications: this.form.controls['email'].value,
      commentNotifications: this.form.controls['comment'].value ? this.form.controls['comment'].value : this.preferences.commentNotifications,
      ratingNotifications: this.form.controls['rating'].value ? this.form.controls['rating'].value : this.preferences.ratingNotifications,
      otherNotifications: this.form.controls['other'].value ? this.form.controls['other'].value : this.preferences.otherNotifications,
      shareHealthWithTrainer: this.form.controls['health'].value,
    }

    this.userService.patchPreferences(newPrefs).subscribe((data) => {
      if (data !== null && data !== undefined) {
        this.preferences = data;
        this.dialogRef.close();
      }
    })
  }

  handleCancel(): void {
    this.dialogRef.close();
  }
}
