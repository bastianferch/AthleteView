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

  editWeeklyConstraint(constraint: TimeConstraint): Observable<TimeConstraint> {
    return this.httpClient.put<TimeConstraint>(this.timeConstraintBaseUri + "/weeklies/" + constraint.id, constraint);
  }

  editDailyConstraint(constraint: TimeConstraint): Observable<TimeConstraint> {
    return this.httpClient.put<TimeConstraint>(this.timeConstraintBaseUri + "/dailies/" + constraint.id, constraint);
  }

  getConstraints(type?: string, from?: string, until?: string): Observable<TimeConstraint[]> {
    let params = new HttpParams()
    if (type) params = params.append("type", type)
    if (from) params = params.append("from", from)
    if (until) params = params.append("until", until)
    return this.httpClient.get<any>(this.timeConstraintBaseUri, { params });
  }

  delete(id: number) {
    return this.httpClient.delete(this.timeConstraintBaseUri + "/" + id);
  }

  getById(id: number): Observable<TimeConstraint> {
    return this.httpClient.get<TimeConstraint>(this.timeConstraintBaseUri + "/" + id);
  }

  sanitize(date: string): string {
    date = date.replace("T", ", ")
    date = date.replace(/Z.*/, ":00")
    return date
  }

}
