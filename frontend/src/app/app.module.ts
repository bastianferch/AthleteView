import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HTTP_INTERCEPTORS, HttpClientModule } from "@angular/common/http";
import { httpInterceptorProviders } from "./domain/auth/service/auth-interceptor";
import { BrowserModule } from "@angular/platform-browser";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { MainModule } from "./domain/main/module/main/main.module";
import { MatSnackBarModule } from '@angular/material/snack-bar';
import {
  ConfirmRegistrationComponent,
} from './domain/auth/component/confirm-registration/confirm-registration.component';
import { MatListModule } from "@angular/material/list";
import { LegalInformationComponent } from './domain/user/component/legal-information/legal-information.component';
import { MatDialogModule } from "@angular/material/dialog";
import { DATE_PIPE_DEFAULT_OPTIONS } from "@angular/common";
import { ForgotPasswordComponent } from './domain/auth/component/forgot-password/forgot-password.component';
import { SharedModule } from "./config/module/SharedModule";
import { ResetPasswordComponent } from './domain/auth/component/reset-password/reset-password.component';
import { SpinnerComponent } from './domain/main/component/spinner/spinner.component';
import { LoadInterceptor } from "./config/load-interceptor";
import { ErrorInterceptor } from "./config/error-interceptor";

@NgModule({
  declarations: [
    AppComponent,
    SpinnerComponent,
    ConfirmRegistrationComponent,
    LegalInformationComponent,
    ForgotPasswordComponent,
    ResetPasswordComponent,
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    HttpClientModule,
    MainModule,
    MatSnackBarModule,
    MatListModule,
    MatDialogModule,
    SharedModule,
  ],
  exports: [],
  providers: [httpInterceptorProviders,
    { provide: DATE_PIPE_DEFAULT_OPTIONS, useValue: { dateFormat: 'shortDate' } },
    { provide: HTTP_INTERCEPTORS, useClass: LoadInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true }],
  bootstrap: [AppComponent],
})
export class AppModule {
}
