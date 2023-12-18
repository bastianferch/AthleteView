import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { HttpClient } from "@angular/common/http";
import { UrlService } from "../../../config/service/UrlService";

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
}
