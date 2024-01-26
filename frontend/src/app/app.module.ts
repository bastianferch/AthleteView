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
import { FitImportDialogComponent } from './fit-import-dialog/fit-import-dialog.component';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { ConfirmationDialogComponent } from './common/component/dialog/confirmation-dialog.component';
import { InviteDialogComponent } from './domain/invite-dialog/invite-dialog.component';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
@NgModule({
  declarations: [
    AppComponent,
    SpinnerComponent,
    ConfirmRegistrationComponent,
    LegalInformationComponent,
    ForgotPasswordComponent,
    ResetPasswordComponent,
    FitImportDialogComponent,
    ConfirmationDialogComponent,
    InviteDialogComponent,
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
    MatFormFieldModule,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
  ],
  exports: [
    SpinnerComponent,
  ],
  providers: [httpInterceptorProviders,
    { provide: DATE_PIPE_DEFAULT_OPTIONS, useValue: { dateFormat: 'shortDate' } },
    { provide: HTTP_INTERCEPTORS, useClass: LoadInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true }],
  bootstrap: [AppComponent],
})
export class AppModule {
}
