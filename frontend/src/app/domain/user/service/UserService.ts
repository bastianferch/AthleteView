import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { UrlService } from "../../../config/service/UrlService";
import { Observable } from "rxjs";
import { Athlete, Trainer, User } from "../dto/user";

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private readonly url;

  constructor(
    private http: HttpClient,
    private urlService: UrlService,
  ) {
    this.url = this.urlService.getBackendUrl() + 'user';
  }

  get(): Observable<User> {
    return this.http.get<User>(
      this.url,
      {
        params: {},withCredentials: true,
      },
    ).pipe(User.serializeResponseMap());
  }

  updateTrainer(trainer: Trainer): Observable<void> {
    return this.http.put<void>(
      this.url + '/trainer',
      trainer,
      {
        params: {},withCredentials: true,
      },
    ).pipe(User.serializeResponseMap());
  }

  updateAthlete(athlete: Athlete): Observable<void> {
    return this.http.put<void>(
      this.url + '/athlete',
      athlete,
      {
        params: {},withCredentials: true,
      },
    ).pipe(User.serializeResponseMap());
  }


}
