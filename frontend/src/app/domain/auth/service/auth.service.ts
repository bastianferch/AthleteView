import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable, tap } from "rxjs";
import { Router } from "@angular/router";
import { UrlService } from "../../config/service/UrlService";
import { LoginDTO } from "../dto/LoginDTO";
import { RegisterDTO } from "../dto/RegisterDTO";
import { User } from "../../user/dto/User";

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly url;

  constructor(private http: HttpClient,
    private urlService: UrlService,
    private router: Router) {
    this.url = this.urlService.getBackendUrl() + 'auth/';
  }

  getAuthToken(): string {
    return localStorage.getItem(JWT_TOKEN_NAME);
  }

  isLoggedIn(): boolean {
    return Boolean(this.getAuthToken());
  }

  login(body: LoginDTO): Observable<User> {
    return this.http.post<User>(this.url + 'login', body, { withCredentials: true }).pipe(
      tap((user) => {
        this.updateCurrentUserSession(user)
      }),
    );
  }

  register(body: RegisterDTO): Observable<User> {
    return this.http.post<User>(this.url + 'registration', body, { withCredentials: true });
  }

  private updateCurrentUserSession(user: User) {
    console.log(user)
    if (!user) {
      return
    }
    localStorage.setItem(JWT_TOKEN_NAME, user.token);
  }

}

export const JWT_TOKEN_NAME = 'auth_token'
