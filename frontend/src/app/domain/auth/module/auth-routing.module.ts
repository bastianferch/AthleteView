import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { LoginComponent } from "../component/login/login.component";
import { UserRegistrationComponent } from "../component/registration/user-registration.component";

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'registration', component: UserRegistrationComponent },
  { path: '', pathMatch: 'full', redirectTo: 'login' },

];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AuthRoutingModule {
}
