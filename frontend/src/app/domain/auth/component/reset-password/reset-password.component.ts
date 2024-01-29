import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from "@angular/forms";
import { ActivatedRoute, Router } from "@angular/router";
import { AuthService } from "../../service/auth.service";
import { ResetPassword } from "../../dto/reset-password";
import { SnackbarService } from "../../../../common/service/snackbar.service";

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.scss'],
})
export class ResetPasswordComponent implements OnInit {
  hidePwd = true;
  hidePwdConfirm = true;

  resetForm: FormGroup<{
    password: FormControl<string>,
    confirmPassword: FormControl<string>,
  }> = this.formBuilder.group(
      {
        password: new FormControl(undefined, {
          validators: [Validators.required, Validators.minLength(8)],
          updateOn: 'change',
        }),
        confirmPassword: ['', { updateOn: 'change' }],
      });

  token: string;

  constructor(
    private route: ActivatedRoute,
    private formBuilder: FormBuilder,
    private snackbarService: SnackbarService,
    private authService: AuthService,
    private router: Router) {
  }

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      this.token = params['uuid'];
    });
  }


  onFormSubmit(): void {
    if (!this.checkPasswordEquality()) {
      return;
    }
    const reset: ResetPassword = {
      password: this.resetForm.value.password,
      token: this.token,
    };
    this.authService.resetPassword(reset).subscribe({
      next: () => {
        this.snackbarService.openSnackBar('Successfully set new password.');
        this.router.navigate(['/auth/login']);
      },
      error: () => {
        this.snackbarService.openSnackBar('The token to reset your password might be expired! Please try sending a mail again!');
      },
    });
  }

  private checkPasswordEquality(): boolean {
    const value = this.resetForm.value;
    if (value.confirmPassword !== value.password) {
      this.resetForm.controls.confirmPassword.setErrors({ matching: 'oof' })
      return false;
    }
    return true;
  }
}
