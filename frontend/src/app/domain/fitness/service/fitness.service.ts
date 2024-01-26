import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { UrlService } from "../../../config/service/UrlService";
import { Observable } from "rxjs";

@Injectable({
  providedIn: 'root',
})
export class FitnessService {
  private readonly url: string;

  constructor(private http: HttpClient,
    private urlService: UrlService) {
    this.url = this.urlService.getBackendUrl() + 'fitness';
  }

  getFitness(targetUserId: number): Observable<number[]> {
    return this.http.get<number[]>(this.url, { params: { targetUserId }, withCredentials: true });
  }

}
