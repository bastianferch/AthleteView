import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from "@angular/forms";
import { AuthService } from "../../service/auth.service";
import { Router } from "@angular/router";
import { RegisterDTO } from "../../dto/RegisterDTO";

@Component({
  selector: 'app-user-registration',
  templateUrl: './user-registration.component.html',
  styleUrls: ['./user-registration.component.scss'],
})
export class UserRegistrationComponent implements OnInit {
  form!: FormGroup<{
    email: FormControl<string>,
    password: FormControl<string>,
    name: FormControl<string>
  }>;

  hide = true;
  acceptedTerms = false;

  constructor(private loginService: AuthService,
    private fb: FormBuilder,
    private router: Router) {
  }

  performRegistration() {
    this.loginService.register(this.form.value as RegisterDTO).subscribe({ next: () => {
      this.router.navigate(['/login'])
    },
    error: () => this.form.controls.email.setErrors({ 'taken': 'oof' }) })
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      email: new FormControl('', { validators: [Validators.required, Validators.email], updateOn: 'change' }),
      password: new FormControl('', { validators: [Validators.required], updateOn: 'change' }),
      name: new FormControl('', { validators: [Validators.required], updateOn: 'change' }),
    });
  }

}
