import {
  HttpContextToken,
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
  HttpResponse,
} from '@angular/common/http';
import { Injectable } from '@angular/core';
import { finalize, Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { AuthService } from "../domain/auth/service/auth.service";
import { SpinnerService } from "../domain/main/service/spinner.service";

@Injectable()
export class LoadInterceptor implements HttpInterceptor {
  constructor(
    private authService: AuthService,
    private spinner: SpinnerService) {
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (request.context.get(IS_SPINNER_ACTIVE)) {
      this.spinner.addActiveRequest();
    }
    return this.handler(next, request);
  }

  handler(next: HttpHandler, request: HttpRequest<any>) {
    return next.handle(request).pipe(
      map((event: HttpEvent<any>) => {
        if (event instanceof HttpResponse) {
          this.authService.setAuthToken(event.headers.get('Authorization'))
        }
        return event;
      }),
      finalize(() => {
        this.spinner.removeActiveRequest()
      }),
    );
  }
}

export const IS_SPINNER_ACTIVE = new HttpContextToken<boolean>(() => true);
