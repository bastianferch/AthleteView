import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { UserInfoComponent } from './domain/user/component/user-info/user-info.component';
import { HttpClientModule } from "@angular/common/http";
import { CommonModule } from "@angular/common";
import { LoginComponent } from "./domain/auth/component/login/login.component";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatIconModule } from "@angular/material/icon";
import { MatInputModule } from "@angular/material/input";
import { MatButtonModule } from "@angular/material/button";
import { UserRegistrationComponent } from "./domain/auth/component/registration/user-registration.component";
import { MatCheckboxModule } from "@angular/material/checkbox";
import { httpInterceptorProviders } from "./domain/auth/service/auth-interceptor";

@NgModule({
  declarations: [
    AppComponent,
    // ToDo: move to separate module.
    UserInfoComponent,
    LoginComponent,
    UserRegistrationComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    BrowserAnimationsModule,
    MatIconModule,
    MatInputModule,
    MatButtonModule,
    FormsModule,
    MatCheckboxModule,
  ],
  providers: [httpInterceptorProviders],
  bootstrap: [AppComponent],
})
export class AppModule { }
