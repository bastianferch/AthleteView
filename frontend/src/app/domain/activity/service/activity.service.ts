import { convertToPlannedActivitySplit, PlannedActivity, PlannedActivitySplit } from '../../../common/dto/PlannedActivity';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { UrlService } from '../../../config/service/UrlService';

@Injectable({
  providedIn: 'root',
})


export class ActivityService {

  private activityBaseUri: string;
  private plannedActivityBaseUri: string;

  constructor(private httpClient: HttpClient, private urlService: UrlService) {
    const backendUrl = this.urlService.getBackendUrl();
    this.activityBaseUri = backendUrl + '/activity';
    this.plannedActivityBaseUri = this.activityBaseUri + '/planned';
  }

  createPlannedActivity(plannedActivity: PlannedActivity): Observable<PlannedActivitySplit> {
    return this.httpClient.post<PlannedActivitySplit>(this.plannedActivityBaseUri, convertToPlannedActivitySplit(plannedActivity));
  }

  editPlannedActivity(plannedActivity: PlannedActivity): Observable<PlannedActivitySplit> {
    return this.httpClient.put<PlannedActivitySplit>(this.plannedActivityBaseUri, convertToPlannedActivitySplit(plannedActivity));
  }
}
