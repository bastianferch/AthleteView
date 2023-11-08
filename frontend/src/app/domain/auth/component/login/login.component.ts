import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from "@angular/forms";
import { LoginDTO } from "../../dto/LoginDTO";
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

  constructor(private loginService: AuthService,
    private fb: FormBuilder,
    private router: Router,
    private authService: AuthService) {
  }

  ngOnInit(): void {
    this.authService.setAuthToken(null);
    this.form = this.fb.group({
      email: new FormControl('', { validators: [Validators.required], updateOn: 'change' }),
      password: new FormControl('', { validators: [Validators.required], updateOn: 'change' }),
    });
  }

  async performLogin() {
    this.loginService.login(this.form.value as LoginDTO).subscribe({
      next: () => {
        this.router.navigate(['/'])
      },
      error: () => {
        this.form.controls.email.setErrors({ 'invalidEmailPassword': 'oof' })
      },
    })
  }

}
