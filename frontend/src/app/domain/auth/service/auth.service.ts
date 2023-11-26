import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from "@angular/common/http";
import { BehaviorSubject, Observable, tap } from "rxjs";
import { UrlService } from "../../../config/service/UrlService";
import { LoginDto } from "../dto/login-dto";
import { RegisterDto } from "../dto/register-dto";
import { User } from "../../user/dto/User";
import { UserType } from "../component/registration/user-registration.component";
import { DateParsing } from "../../../common/util/parsing/date-parsing";
import { ResetPassword } from "../dto/reset-password";

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly url;
  private currentUser$ = new BehaviorSubject<User>(undefined);

  currentUser: User;
  getCurrentUser$ = this.currentUser$.asObservable();

  constructor(private http: HttpClient,
    private urlService: UrlService) {
    this.url = this.urlService.getBackendUrl() + 'auth/';
  }

  setCurrentUser(user: User): void {
    this.currentUser = user;
    this.currentUser$.next(user);
  }

  logout(): void {
    this.setAuthToken(null);
    this.setCurrentUser(null)
  }

  setAuthToken(token: string) {
    localStorage.setItem(JWT_TOKEN_NAME, token);
  }

  getAuthToken(): string {
    return localStorage.getItem(JWT_TOKEN_NAME);
  }

  // ---- router methods ----

  login(body: LoginDto): Observable<User> {
    return this.http.post<User>(this.url + 'login', body, { withCredentials: true }).pipe(
      tap((user) => {
        this.setCurrentUser(user)
        this.setAuthToken(user.token)
      }),
    );
  }

  confirmAccount(uuid: string): Observable<void> {
    const params = new HttpParams().set('token', uuid);
    return this.http.post<void>(this.url + 'confirmation', null, {
      params,
    });
  }

  sendConfirmLink(body: LoginDto): Observable<void> {
    return this.http.post<void>(this.url + 'confirmation/new', body);
  }

  register(body: RegisterDto, type: UserType): Observable<User> {
    if (body.dob) {
      body.dob = new DateParsing().toHyphenDate(new Date(body.dob));
    }
    body.height = Math.ceil(body.height * 1000); // convert from meter to mm
    body.weight = Math.ceil(body.weight * 1000); // convert from kg to g
    return this.http.post<User>(this.url + 'registration/' + type, body, { withCredentials: true });
  }

  forgotPassword(email: string): Observable<void> {
    return this.http.post<void>(this.url + 'forgot-password', email);
  }

  resetPassword(wrapper: ResetPassword): Observable<void> {
    return this.http.post<void>(this.url + 'password', wrapper);
  }

}

export const JWT_TOKEN_NAME = 'auth_token'
