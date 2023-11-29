import { PlannedActivitySplit } from '../dto/PlannedActivity';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { UrlService } from '../../../config/service/UrlService';
import { SnackbarService } from "../../../common/service/snackbar.service";
import { IntervalSplit } from "../../../common/interval/dto/Interval";

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

  // do some post-processing on the activity
  // if it is a template, set the date to null
  postProcessActivity(activity: PlannedActivitySplit) {
    if (activity.template) {
      activity.date = null;
    }

    return activity;
  }

  // TODO also validate all these things in the backend!
  validateActivity(activity: PlannedActivitySplit) {
    // TODO check if the backend allows more chars
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
    // TODO check if the backend allows more chars
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
}
