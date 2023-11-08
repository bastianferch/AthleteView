import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { BehaviorSubject, Observable, tap } from "rxjs";
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

  // ToDo: make a BehaviorSubject from it.
  isLoggedIn(): boolean {
    const token = this.getAuthToken();
    return token && token !== 'null';
  }

  logout(): void {
    this.setAuthToken(null);
  }

  login(body: LoginDTO): Observable<User> {
    return this.http.post<User>(this.url + 'login', body, { withCredentials: true }).pipe(
      tap((user) => {
        this.setAuthToken(user.token)
      }),
    );
  }

  register(body: RegisterDTO): Observable<User> {
    return this.http.post<User>(this.url + 'registration', body, { withCredentials: true });
  }

  setAuthToken(token: string) {
    localStorage.setItem(JWT_TOKEN_NAME, token);
  }

}

export const JWT_TOKEN_NAME = 'auth_token'
