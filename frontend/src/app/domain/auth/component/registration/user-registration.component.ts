import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from "@angular/forms";
import { AuthService } from "../../service/auth.service";
import { ActivatedRoute, Router } from "@angular/router";
import { RegisterDto } from "../../dto/register-dto";
import { LegalInformationComponent } from "../../../user/component/legal-information/legal-information.component";
import { MatDialog } from "@angular/material/dialog";
import { SnackbarService } from "../../../../common/service/snackbar.service";

@Component({
  selector: 'app-user-registration',
  templateUrl: './user-registration.component.html',
  styleUrls: ['./user-registration.component.scss'],
})
export class UserRegistrationComponent implements OnInit {
  form!: FormGroup<{
    email: FormControl<string>,
    password: FormControl<string>,
    name: FormControl<string>,
    country: FormControl<string>,
    zip: FormControl<string>,
    dob: FormControl<Date>
    height: FormControl<number>,
    weight: FormControl<number>,
    code: FormControl<string>
  }>;

  userType: UserType = 'athlete'

  hidePassword = true;
  acceptedTerms = false;
  code : string;

  constructor(
    private loginService: AuthService,
    private fb: FormBuilder,
    private router: Router,
    private authService: AuthService,
    private dialog: MatDialog,
    private route: ActivatedRoute,
    private snackbarService: SnackbarService) {
  }

  openLegalDialog(): void {
    this.dialog.open(LegalInformationComponent, {
      height: '400px',
      width: '600px',
    });
  }

  updateUserType(userType: UserType): void {
    if (userType === 'athlete') {
      this.form.controls.dob.setValidators([Validators.required]);
      this.form.controls.weight.setValidators([Validators.required, Validators.min(0), Validators.max(700)]);
      this.form.controls.height.setValidators([Validators.required, Validators.min(0), Validators.max(3)]);
    } else {
      this.form.controls.dob.clearValidators();
      this.form.controls.weight.clearValidators();
      this.form.controls.height.clearValidators();
    }
    this.form.controls.dob.updateValueAndValidity();
    this.form.controls.weight.updateValueAndValidity();
    this.form.controls.height.updateValueAndValidity();
    this.userType = userType;
  }

  performRegistration() {
    const body = this.form.value as RegisterDto;
    this.loginService.register(body, this.userType).subscribe({
      next: () => {
        this.snackbarService.openSnackBar("Registration successful! Please Confirm your Email address!")
        this.router.navigate(['/auth/login'])
      },
      error: (err) => {
        if (err.status === 409) {
          this.form.controls.email.setErrors({ 'taken': 'oof' })
        } else if (err.status === 422) {

          if (err.error?.message?.includes('years old')) {
            this.form.controls.dob.setErrors({ 'old': 'oof' })
          } else if (err.error?.message?.includes('Could not parse a date')) {
            this.form.controls.dob.setErrors({ 'invalid_dob': 'oof' })
          } else {
            this.form.controls.email.setErrors({ 'email': 'oof' })
          }
        }
      },
    })
  }

  ngOnInit(): void {
    this.authService.logout();
    this.code = this.route.snapshot.paramMap.get('code');
    this.form = this.fb.group({
      email: new FormControl(undefined, { validators: [Validators.required, Validators.email], updateOn: 'change' }),
      password: new FormControl(undefined, {
        validators: [Validators.required, Validators.minLength(8), Validators.maxLength(255)],
        updateOn: 'change',
      }),
      name: new FormControl(undefined, { validators: [Validators.required].concat(defaultMinMaxValidator), updateOn: 'change' }),
      country: new FormControl(undefined, { validators: defaultMinMaxValidator,updateOn: 'change' }),
      zip: new FormControl(undefined, { validators: defaultMinMaxValidator, updateOn: 'change' }),
      height: new FormControl(undefined, { updateOn: 'change' }),
      dob: new FormControl(undefined, { updateOn: 'change' }),
      weight: new FormControl(undefined, { updateOn: 'change' }),
      code: new FormControl(this.code),
    });
    this.updateUserType('athlete')
  }

}

export type UserType = 'trainer' | 'athlete'
export const defaultMinMaxValidator = [Validators.maxLength(255), Validators.minLength(0)]
