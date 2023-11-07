import { RouterModule, Routes } from "@angular/router";
import { NgModule } from "@angular/core";
import { UserInfoComponent } from "./component/user-info/user-info.component";

const routes: Routes = [
  { path: '', component: UserInfoComponent },
];

@NgModule({
  declarations: [UserInfoComponent],
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class UserModule {}
