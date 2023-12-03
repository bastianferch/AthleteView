import { PlannedActivity, PlannedActivitySplit } from '../dto/PlannedActivity';
import { Observable } from 'rxjs';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { UrlService } from '../../../config/service/UrlService';
import { SnackbarService } from "../../../common/service/snackbar.service";
import { IntervalSplit } from "../../../common/interval/dto/Interval";
import { Activity } from "../../activity/dto/Activity"
import { NavigationExtras } from '@angular/router';
import { query } from '@angular/animations';


@Injectable({
  providedIn: 'root',
})
export class ActivityService {
  private activityBaseUri: string;
  private plannedActivityBaseUri: string;

  constructor(
    private httpClient: HttpClient,
    private urlService: UrlService,
    private snackbarService: SnackbarService) {
    const backendUrl = this.urlService.getBackendUrl();
    this.activityBaseUri = backendUrl + 'activity';
    this.plannedActivityBaseUri = this.activityBaseUri + '/planned';
  }

  createPlannedActivity(plannedActivity: PlannedActivitySplit): Observable<PlannedActivitySplit> {
    return this.httpClient.post<PlannedActivitySplit>(this.plannedActivityBaseUri, plannedActivity);
  }

  editPlannedActivity(plannedActivity: PlannedActivitySplit): Observable<PlannedActivitySplit> {
    return this.httpClient.put<PlannedActivitySplit>(this.plannedActivityBaseUri + '/' + plannedActivity.id, plannedActivity);
  }

  getPlannedActivity(id: number) {
    return this.httpClient.get<PlannedActivitySplit>(this.plannedActivityBaseUri + '/' + id);
  }

  importFitActivity(data: File[]) {
    const url = this.activityBaseUri + '/import'
    const formData = new FormData()
    for (const item of data) {
      formData.append("files", item)
    }
    return this.httpClient.post(url, formData)
  }

  fetchAllActivitiesForUser(uid: number, startTime: string, endTime: string) {
    const params = this.buildTimeParams(startTime, endTime)
    const url = this.activityBaseUri + "/finished"
    return this.httpClient.get<Array<Activity>>(url, { params: params })
  }

  fetchAllPlannedActivitiesForUser(uid: number, startTime: string, endTime: string) {
    const params = this.buildTimeParams(startTime, endTime)
    const url = this.activityBaseUri + "/planned"
    return this.httpClient.get<Array<PlannedActivity>>(url, { params: params })
  }


  // do some post-processing on the activity
  // if it is a template, set the date to null
  postProcessActivity(activity: PlannedActivitySplit) {
    if (activity.template) {
      activity.date = null;
    }

    return activity;
  }

  validateActivity(activity: PlannedActivitySplit) {
    const maxNoteLength = 255;

    if (activity.date === null && activity.template === false) {
      this.snackbarService.openSnackBar("Activity must either have a date or be a template")
      return false;
    }

    if (activity.note.length > maxNoteLength) {
      this.snackbarService.openSnackBar(`Notes cannot be longer than ${maxNoteLength} characters`)
      return false;
    }

    if (!this.validateInterval(activity.interval)) {
      return false;
    }

    // TODO if the logged in user is a trainer, check that only templates are created!

    // TODO if the logged in user is an athlete, check that the activity has withTrainer === false

    return true;
  }

  private validateInterval(interval: IntervalSplit): boolean {
    const maxNoteLength = 255;

    if (interval.intervals && interval.intervals.length !== 0) {
      // check all sub-intervals. If there is an error in one, return false
      for (const i of interval.intervals) {
        if (!this.validateInterval(i)) return false;
      }
      // if all sub-intervals are ok, return true
      return true;
    }
    if (interval.step) {
      // if this interval contains a step, validate it
      const step = interval.step;
      if (step.note && step.note.length > maxNoteLength) {
        this.snackbarService.openSnackBar(`Notes cannot be longer than ${maxNoteLength} characters`);
        return false
      }
      // TODO other validations
      return true;
    }
    return false;
  }

  private buildTimeParams(startTime: string, endTime: string): HttpParams {
    let params = new HttpParams()
    if (startTime != null) {
      params = params.append("startTime", startTime)
    }
    if (endTime != null) {
      params = params.append("endTime", endTime)
    }

    return params
  }
}
