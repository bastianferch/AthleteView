/* eslint-disable no-console */
import { HttpContextToken, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { SpinnerService } from "../domain/main/service/spinner.service";
import { Router } from "@angular/router";

export const IGNORE_ERROR_HANDLING = new HttpContextToken<boolean>(() => false);

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  constructor(
    private spinner: SpinnerService,
    private router: Router,
  ) {
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return req.context?.get(IGNORE_ERROR_HANDLING) ?
      next.handle(req).pipe(catchError(this.handleError.bind(this))) :
      next.handle(req).pipe(catchError(this.handleErrorWithNotification.bind(this)));
  }

  handleError(error:any) {
    this.spinner.resetActiveRequests();
    if (error.status === 403) {
      this.router.navigate(['/auth/login'])
    }
    return throwError(error);
  }

  handleErrorWithNotification(error:any) {
    // notification service should notify the error.
    return this.handleError(error);
  }
}
