import { WeeklyTimeConstraint, DailyTimeConstraint } from '../../../common/dto/TimeConstraint';
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

  createWeeklyConstraint(constraint: WeeklyTimeConstraint): Observable<WeeklyTimeConstraint> {
    return this.httpClient.post<WeeklyTimeConstraint>(this.timeConstraintBaseUri + "/weeklies", constraint);
  }

  createDailyConstraint(constraint: DailyTimeConstraint): Observable<DailyTimeConstraint> {
    return this.httpClient.post<DailyTimeConstraint>(this.timeConstraintBaseUri + "/dailies", constraint);
  }

  getConstraints(type?: string, from?: string): Observable<any> {
    let params = new HttpParams()
    if (type) params = params.append("type", type)
    if (from) params = params.append("from", from)
    return this.httpClient.get<any>(this.timeConstraintBaseUri, {params});
  }

}
