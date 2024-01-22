import { PlannedActivity, PlannedActivitySplit } from '../dto/PlannedActivity';
import { Observable, tap } from 'rxjs';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { UrlService } from '../../../config/service/UrlService';
import { SnackbarService } from "../../../common/service/snackbar.service";
import { IntervalSplit } from "../../../common/interval/dto/Interval";
import { Activity } from '../dto/Activity'
import { DateParsing } from 'src/app/common/util/parsing/date-parsing';
import { CommentDTO } from "../dto/Comment";

@Injectable({
  providedIn: 'root',
})
export class ActivityService {
  private activityBaseUri: string;
  private plannedActivityBaseUri: string;

  constructor(
    private httpClient: HttpClient,
    private urlService: UrlService,
    private snackbarService: SnackbarService,
    private dateParser: DateParsing,
  ) {
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
      .pipe(tap((item) => {
        const newData = []

        for (const x of item) {
          if (x.startTime !== null && x.startTime !== undefined) {
            x.startTime = this.dateParser.parseNumbersIntoDate(x.startTime as number[])
          }

          if (x.endTime !== null && x.endTime !== undefined) {
            x.endTime = this.dateParser.parseNumbersIntoDate(x.endTime as number[])
          }

          newData.push(x)
        }
        return newData
      }))
  }

  fetchAllPlannedActivitiesForUser(uid: number, startTime: string, endTime: string) {
    const params = this.buildTimeParams(startTime, endTime)
    const url = this.activityBaseUri + "/planned"
    return this.httpClient.get<Array<PlannedActivity>>(url, { params: params })
      .pipe(tap((item) => {
        const newData = []

        for (const x of item) {
          if (x.date !== null && x.date !== undefined) {
            x.date = this.dateParser.parseNumbersIntoDate(x.date as number[])
          }

          newData.push(x)
        }

        return newData
      }))
  }

  fetchActivityForUser(aid: number): Observable<Activity> {
    const url = this.activityBaseUri + `/finished/${aid}`
    return this.httpClient.get<Activity>(url)
      .pipe(tap((item) => {
        if (item.startTime !== undefined) {
          item.startTime = this.dateParser.parseNumbersIntoDate(item.startTime as number[])
        }

        if (item.endTime !== undefined) {
          item.endTime = this.dateParser.parseNumbersIntoDate(item.endTime as number[])
        }

        return item
      }))
  }

  commentActivity(aid: number, comment: string) {
    return this.httpClient.patch<CommentDTO>(
      this.activityBaseUri + `/finished/${aid}`,
      {
        id: null,
        text: comment,
        author: null,
        date: null,
      },
      { withCredentials: true },
    );
  }

  rateActivity(aid: number, rating: number) {
    return this.httpClient.patch(
      this.activityBaseUri + `/finished/rate/${aid}/${rating}`,
      null,
      { withCredentials: true },
    );
  }

  // do some post-processing on the activity
  // if it is a template, set the date to null
  postProcessActivity(activity: PlannedActivitySplit) {
    if (activity.template) {
      activity.date = null;
      activity.createdFor = null;
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
      return true;
    }
    return false;
  }

  private buildTimeParams(startTime: string, endTime: string): HttpParams {
    let params = new HttpParams()
    if (startTime !== null) {
      params = params.append("startTime", startTime)
    }
    if (endTime !== null) {
      params = params.append("endTime", endTime)
    }

    return params
  }

  private parseDate(numbers: number[]): any {
    const str: string[] = []
    numbers.forEach((num) => {
      if (num.toString().length === 1) {
        str.push("0" + num.toString())
      } else {
        str.push(num.toString())
      }
    });
    return str[0] + "-" + str[1] + "-" + str[2] + "T" + str[3] + ":" + str[4]
  }
}
