import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { LoginComponent } from "../component/login/login.component";
import { UserRegistrationComponent } from "../component/registration/user-registration.component";
import { ConfirmRegistrationComponent } from "../component/confirm-registration/confirm-registration.component";
import { ForgotPasswordComponent } from "../component/forgot-password/forgot-password.component";
import { ResetPasswordComponent } from "../component/reset-password/reset-password.component";

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'registration', component: UserRegistrationComponent },
  { path: 'registration/:code', component: UserRegistrationComponent },
  { path: 'confirm/:uuid', component: ConfirmRegistrationComponent },
  { path: 'email-recovery', component: ForgotPasswordComponent },
  { path: 'reset/:uuid', component: ResetPasswordComponent },
  { path: '', pathMatch: 'full', redirectTo: 'login' },

];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AuthRoutingModule {
}
