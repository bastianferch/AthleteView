import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { UserInfoComponent } from "./domain/user/component/user-info/user-info.component";
import { LoginComponent } from "./domain/auth/component/login/login.component";
import { authGuard } from "./domain/config/auth.guard";
import { UserRegistrationComponent } from "./domain/auth/component/registration/user-registration.component";

// ToDo make it lazy.
// ToDo add extra modules.
// ToDo change auth guard.
const routes: Routes = [
  { path: '', canActivate: [authGuard], component: UserInfoComponent },
  { path: 'login', component: LoginComponent },
  { path: 'registration', component: UserRegistrationComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule { }
