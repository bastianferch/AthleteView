import { TimeConstraint } from '../../../common/dto/TimeConstraint';
import { Observable } from 'rxjs';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { UrlService } from '../../../config/service/UrlService';

@Injectable({
  providedIn: 'root',
})


export class TimeConstraintService {

  private timeConstraintBaseUri: string;

  constructor(private httpClient: HttpClient, private urlService: UrlService) {
    const backendUrl = this.urlService.getBackendUrl();
    this.timeConstraintBaseUri = backendUrl + 'constraints';
  }

  createWeeklyConstraint(constraint: TimeConstraint): Observable<TimeConstraint> {
    return this.httpClient.post<TimeConstraint>(this.timeConstraintBaseUri + "/weeklies", constraint);
  }

  createDailyConstraint(constraint: TimeConstraint): Observable<TimeConstraint> {
    return this.httpClient.post<TimeConstraint>(this.timeConstraintBaseUri + "/dailies", constraint);
  }

  getConstraints(type?: string, from?: string): Observable<TimeConstraint[]> {
    let params = new HttpParams()
    if (type) params = params.append("type", type)
    if (from) params = params.append("from", from)
    return this.httpClient.get<any>(this.timeConstraintBaseUri, { params });
  }

  delete(id: number) {
    return this.httpClient.delete(this.timeConstraintBaseUri + "/" + id);
  }

}
