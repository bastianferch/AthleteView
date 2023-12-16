import { Component, OnInit } from '@angular/core';
import { UserService } from "../../service/UserService";
import { AuthService } from "../../../auth/service/auth.service";
import { Athlete, Trainer, User } from "../../dto/user";
import { FormBuilder, FormControl, FormGroup, Validators } from "@angular/forms";
import { defaultMinMaxValidator } from "../../../auth/component/registration/user-registration.component";
import { SnackbarService } from "../../../../common/service/snackbar.service";
import { PreferencesDto } from "../../dto/preferences-dto";

@Component({
  selector: 'app-user-info',
  templateUrl: './user-info.component.html',
  styleUrls: ['./user-info.component.scss'],
})
export class UserInfoComponent implements OnInit {
  user: User;
  preferences: PreferencesDto;
  form!: UserInfoFormGroup;

  constructor(private userService: UserService,
    private authService: AuthService, private fb: FormBuilder,
    private notificationService: SnackbarService,
  ) {
  }

  ngOnInit(): void {
    this.getPreferences();
    this.user = this.authService.currentUser;
    const form: UserInfoFormGroup = this.fb.group({
      email: new FormControl({ value: this.user.email, disabled: true }, { updateOn: 'change' }),
      name: new FormControl(this.user.name, {
        validators: [Validators.required].concat(defaultMinMaxValidator),
        updateOn: 'change',
      }),
      country: new FormControl(this.user.country, { validators: defaultMinMaxValidator, updateOn: 'change' }),
      zip: new FormControl(this.user.zip, { validators: defaultMinMaxValidator, updateOn: 'change' }),
    });
    if (this.user.isAthlete()) {
      const athlete = this.user as Athlete;
      form.addControl('height', new FormControl(athlete.height / 1000, { updateOn: 'change', validators: [Validators.required, Validators.min(0), Validators.max(3)] }));
      form.addControl('dob', new FormControl(new Date(athlete.dob), { updateOn: 'change', validators: [Validators.required] }));
      form.addControl('weight', new FormControl(athlete.weight / 1000, { updateOn: 'change', validators: [Validators.required, Validators.min(0), Validators.max(700)] }));
    } else {
      const trainer = this.user as Trainer;
      form.addControl('code', new FormControl(trainer.code, { updateOn: 'change' }));
    }
    this.form = form;
  }

  resetPassword(): void {
    this.authService.forgotPassword(this.user.email).subscribe({
      next: () => {
        this.notificationService.openSnackBar('The message was sent to your email.')
      },
    })
  }

  performUpdate(): void {
    // update preferences like email notifications
    this.patchPreferences();

    // update the actual user data
    if (this.user.isAthlete()) {
      this.updateAthlete();
    } else {
      this.updateTrainer();
    }
  }

  private getPreferences() {
    this.userService.getPreferences().subscribe((data) => {
      if (data !== null && data !== undefined) {
        this.preferences = data;
      }
    })
  }

  private patchPreferences() {
    this.userService.patchPreferences(this.preferences).subscribe((data) => {
      if (data !== null && data !== undefined) {
        this.preferences = data;
      }
    })
  }

  private updateAthlete(): void {
    const athlete = this.form.getRawValue() as Athlete;
    athlete.weight *= 1000;
    athlete.height *= 1000;
    this.userService.updateAthlete(athlete).subscribe({
      next: () => {
        this.notificationService.openSnackBar('Athlete ' + athlete.name + ' was updated successfully.')
      }, error: (err) => {
        this.parseError(err, athlete.name);
      },
    });
  }

  private updateTrainer(): void {
    const trainer = this.form.getRawValue() as Trainer
    this.userService.updateTrainer(trainer).subscribe({
      next: () => {
        this.notificationService.openSnackBar('Trainer ' + trainer.name + ' was updated successfully.')
      }, error: (err) => {
        this.parseError(err, trainer.name);
      },
    })
  }

  private parseError(err: any, name: string): void {
    this.notificationService.openSnackBar('Could not update athlete ' + name + '. Please check the inputs.')
    if (err.status === 422) {
      if (err.error?.message?.includes('years old')) {
        this.form.controls.dob.setErrors({ 'old': 'oof' })
      } else if (err.error?.message?.includes('Could not parse a date')) {
        this.form.controls.dob.setErrors({ 'invalid_dob': 'oof' })
      } else {
        this.form.controls.email.setErrors({ 'email': 'oof' })
      }
    }
  }

}

type UserInfoFormGroup = FormGroup<{
  email: FormControl<string>,
  name: FormControl<string>,
  country: FormControl<string>,
  zip: FormControl<string>,
  dob?: FormControl<Date>
  height?: FormControl<number>,
  weight?: FormControl<number>,
  code?: FormControl<string>,
}>
