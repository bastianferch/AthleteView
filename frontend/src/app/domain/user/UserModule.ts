import { RouterModule, Routes } from "@angular/router";
import { NgModule } from "@angular/core";
import { UserInfoComponent } from "./component/user-info/user-info.component";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { MatButtonModule } from "@angular/material/button";
import { MatCheckboxModule } from "@angular/material/checkbox";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatIconModule } from "@angular/material/icon";
import { MatInputModule } from "@angular/material/input";
import { MatRadioModule } from "@angular/material/radio";
import { NgForOf, NgIf } from "@angular/common";
import { MatNativeDateModule } from "@angular/material/core";
import { MatSlideToggleModule } from "@angular/material/slide-toggle";

const routes: Routes = [
  { path: '', component: UserInfoComponent },
];

@NgModule({
  declarations: [UserInfoComponent],
  imports: [RouterModule.forChild(routes),
    FormsModule,
    MatButtonModule,
    MatCheckboxModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatRadioModule,
    NgForOf,
    NgIf,
    MatNativeDateModule,
    ReactiveFormsModule, MatSlideToggleModule],
  exports: [RouterModule],
})
export class UserModule {}
