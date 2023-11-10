import { NgModule } from '@angular/core';
import { LoginComponent } from "../component/login/login.component";
import { UserRegistrationComponent } from "../component/registration/user-registration.component";
import { AuthRoutingModule } from "./auth-routing.module";
import { SharedModule } from "../../../config/module/SharedModule";


@NgModule({
  declarations: [
    LoginComponent,
    UserRegistrationComponent,
  ],
  imports: [
    AuthRoutingModule,
    SharedModule,
  ],
})
export class AuthModule { }
