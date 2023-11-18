import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { AuthService } from "../domain/auth/service/auth.service";

@Injectable()
export class LoadInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService) {
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
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
    );
  }
}
