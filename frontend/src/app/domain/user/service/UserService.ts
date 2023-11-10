import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { UrlService } from "../../../config/service/UrlService";
import { Observable } from "rxjs";
import { User } from "../dto/User";

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

  createTestData(): Observable<User> {
    return this.http.post<User>(
      this.url,
      null,
      {
        params: {},
        withCredentials: true, responseType: 'json',
      },
    );
  }


}
