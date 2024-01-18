import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { HttpClient } from "@angular/common/http";
import { UrlService } from "../../../config/service/UrlService";
import { Health } from "../../../common/dto/Health";

@Injectable({
  providedIn: 'root',
})
export class HealthService {
  private readonly url;

  constructor(
    private http: HttpClient,
    private urlService: UrlService) {
    this.url = this.urlService.getBackendUrl() + 'health/';
  }

  mock(): Observable<void> {
    return this.http.post<void>(
      this.url + 'mock', null, { withCredentials: true },
    );
  }

  get(): Observable<Health> {
    return this.http.get<Health>(
      this.url + 'stats', { withCredentials: true },
    );
  }

  getFromAthlete(id: number): Observable<Health> {
    return this.http.get<Health>(
      this.url + 'stats/' + id, { withCredentials: true },
    );
  }
}
