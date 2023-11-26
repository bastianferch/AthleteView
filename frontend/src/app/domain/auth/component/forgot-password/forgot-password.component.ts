import { Component } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from "@angular/forms";
import { AuthService } from "../../service/auth.service";
import { Router } from "@angular/router";

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.scss'],
})
export class ForgotPasswordComponent {
  forgotForm: FormGroup<{ email: FormControl<string> }> = this.formBuilder.group(
    {
      email: ['', { validators: [Validators.required, Validators.email], updateOn: 'change' }],
    });


  constructor(private formBuilder: FormBuilder,
    private authService: AuthService,
    private router: Router) {
  }

  onFormSubmit(): void {
    this.authService.forgotPassword(this.forgotForm.value.email).subscribe({
      complete: () => {
        // this.notificationService.success('In case such user exists in the system, the email was sent.');
        this.router.navigate(['/']);
      },
    });
  }
}
