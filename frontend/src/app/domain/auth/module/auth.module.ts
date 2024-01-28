import { NgModule } from '@angular/core';
import { LoginComponent } from "../component/login/login.component";
import { UserRegistrationComponent } from "../component/registration/user-registration.component";
import { AuthRoutingModule } from "./auth-routing.module";
import { SharedModule } from "../../../config/module/SharedModule";
import { MatTabsModule } from "@angular/material/tabs";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { MatNativeDateModule } from "@angular/material/core";
import { MatRadioModule } from "@angular/material/radio";
import { FormsModule } from "@angular/forms";
import { PasswordStrengthMeterModule } from "angular-password-strength-meter";
import { DEFAULT_PSM_OPTIONS } from 'angular-password-strength-meter/zxcvbn';


@NgModule({
  declarations: [
    LoginComponent,
    UserRegistrationComponent,
  ],
  imports: [
    AuthRoutingModule,
    SharedModule,
    MatTabsModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatRadioModule,
    FormsModule,
    PasswordStrengthMeterModule.forRoot(DEFAULT_PSM_OPTIONS),
  ],
})
export class AuthModule { }
