import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from "@angular/forms";
import { LoginDto } from "../../dto/login-dto";
import { Router } from "@angular/router";
import { AuthService } from "../../service/auth.service";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent implements OnInit {
  form!: FormGroup<{
    email: FormControl,
    password: FormControl,
  }>;

  hide = true;
  isAccountActivated = true;
  accountForActivation: LoginDto;

  constructor(private loginService: AuthService,
    private fb: FormBuilder,
    private router: Router,
    private authService: AuthService) {
  }

  ngOnInit(): void {
    this.authService.logout()
    this.form = this.fb.group({
      email: new FormControl('', { validators: [Validators.required], updateOn: 'change' }),
      password: new FormControl('', { validators: [Validators.required], updateOn: 'change' }),
    });
  }

  sendConfirmLink(): void {
    this.loginService.sendConfirmLink(this.accountForActivation).subscribe({
      next: () => {
        this.accountForActivation = undefined;
        this.isAccountActivated = true;
      },
    })
  }

  async performLogin() {
    this.isAccountActivated = true;
    this.loginService.login(this.form.value as LoginDto).subscribe({
      next: () => {
        this.router.navigate(['/'])
      },
      error: (err) => {
        if (err.status === 409) {
          this.isAccountActivated = false;
          this.accountForActivation = this.form.value as LoginDto;
        } else {
          this.form.controls.email.setErrors({ 'invalidEmailPassword': 'oof' })
        }
      },
    })
  }

}
