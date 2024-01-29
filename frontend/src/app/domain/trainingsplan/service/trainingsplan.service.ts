import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { UrlService } from "../../../config/service/UrlService";
import { addWeeks, endOfWeek, format, startOfWeek } from "date-fns";
import { Observable } from "rxjs";
import { User } from "../dto/user";
import { convertLoadToInt, Load, PlannedActivity } from "../../activity/dto/PlannedActivity";

@Injectable({
  providedIn: 'root',
})
export class TrainingsplanService {
  private readonly userBaseUrl: string;
  private readonly userAthleteBaseUri: string;
  private readonly activityBaseUrl: string;
  private readonly allPreviousActivityBaseUri:string;
  private readonly templateActivitiesBaseUri:string;
  private readonly cspScheduleUri: string;
  private currentIndex = 0;
  private keyPrefix = 'trainingsPlan ';
  private athletesKey:string = this.keyPrefix + "athletes";
  private previousActivitiesKey = this.keyPrefix + "previous_activities";
  private upcomingActivitiesSuffix = "upcoming_activities";

  constructor(
    private httpClient:HttpClient,
    private urlService: UrlService,
  ) {
    const backendUrl = this.urlService.getBackendUrl();
    this.userBaseUrl = backendUrl + 'user';
    this.userAthleteBaseUri = this.userBaseUrl + '/athlete';
    this.activityBaseUrl = backendUrl + "activity"
    this.allPreviousActivityBaseUri = this.activityBaseUrl + "/planned";
    this.templateActivitiesBaseUri = this.activityBaseUrl + "/templates";
    this.cspScheduleUri = backendUrl + "csp";
  }

  /**
   * Fetches all athletes linked to the logged in trainer as User DTOs
   */
  fetchAthletesForTrainer():Observable<User[]> {
    return this.httpClient.get<User[]>(this.userAthleteBaseUri,{ params: {},withCredentials: true });
  }

  /**
   * Puts a list of UserDTO into the session storage
   * under the key this.athletesKey
   * @param athletes
   */
  updateAthletesInSessionStorage(athletes:User[]):void {
    const serializedStorageItem = JSON.stringify(athletes);
    sessionStorage.setItem(this.athletesKey,serializedStorageItem);
  }

  /**
   * Fetches all previous (last week) activities for all athletes linked to the logged in trainer
   * as a list of PlannedActivity DTOs
   */
  fetchPreviousActivitiesForAllAthletes():Observable<PlannedActivity[]> {
    const viewDate: Date = new Date();
    const dateFormatString = "yyyy-MM-dd'T'HH:mm:ssxxx"; // ISO format
    const startTime = format(startOfWeek(viewDate, { weekStartsOn: 1 }), dateFormatString);
    const endTime = format(endOfWeek(viewDate,{ weekStartsOn: 1 }), dateFormatString);
    return this.httpClient.get<PlannedActivity[]>(this.allPreviousActivityBaseUri,
      { params: { 'startTime': startTime,
        'endTime': endTime,
        withCredentials: true } },
    );
  }

  /**
   * Puts a list of PlannedActivity DTOs into the session storage
   * under the key this.previousActivitiesKey
   * @param activities
   */
  updatePreviousActivitiesInSessionStorage(activities:PlannedActivity[]):void {
    const serializedStorageItem = JSON.stringify(activities);
    sessionStorage.setItem(this.previousActivitiesKey,serializedStorageItem);
  }

  /**
   * Returns the previous activities of the currently "selected" athlete
   * if existent in sessions storage
   * else returns null
   */
  getPreviousActivitiesForCurrentAthlete():PlannedActivity[] {
    const currentAthlete = this.getCurrentAthlete().id;
    if (sessionStorage.getItem(this.previousActivitiesKey)) {
      const activities = JSON.parse(sessionStorage.getItem(this.previousActivitiesKey));
      const ret:PlannedActivity[] = [];
      activities.forEach((a:PlannedActivity) => {
        if (a.createdFor?.id === currentAthlete) {
          ret.push(a);
        } else if (a.createdBy.id === currentAthlete) {
          ret.push(a);
        }
      })
      return ret;
    }
    return null
  }

  /**
   * Fetches all upcoming (next week) activities for all athletes linked to the logged in trainer
   * as a list of PlannedActivity DTOs
   */
  fetchUpcomingActivitiesForAllAthletes():Observable<PlannedActivity[]> {
    const viewDate: Date = new Date();
    const dateFormatString = "yyyy-MM-dd'T'HH:mm:ssxxx"; // ISO format
    const startTime = format(addWeeks(startOfWeek(viewDate,{ weekStartsOn: 1 }), 1), dateFormatString);
    const endTime = format(addWeeks(endOfWeek(viewDate,{ weekStartsOn: 1 }), 1), dateFormatString);
    return this.httpClient.get<PlannedActivity[]>(this.allPreviousActivityBaseUri,
      { params: { 'startTime': startTime,
        'endTime': endTime,
        withCredentials: true } },
    )
  }

  /**
   * Updates the planned activity list in session storage for each athlete
   * @param upcomingActivities a list of PlannedActivity to be written to ss
   */
  updateUpcomingActivities(upcomingActivities:PlannedActivity[]) {
    let athletes;
    if (sessionStorage.getItem(this.athletesKey)) {
      athletes = JSON.parse(sessionStorage.getItem(this.athletesKey));
    } else {
      athletes = [];
    }
    const activities:any = {};
    for (const athlete of athletes) {
      activities[athlete.id] = [];
      for (const activity of upcomingActivities) {
        if (activity.createdFor.id === athlete.id || activity.createdBy.id === athlete.id) {
          activities[athlete.id].push(activity)
        }
      }
    }
    for (const athlete of athletes) {
      const tmp = activities[athlete.id];
      const serializedStorageItem = JSON.stringify(tmp);
      if (!sessionStorage.getItem(this.keyPrefix + athlete.id + this.upcomingActivitiesSuffix)) {
        sessionStorage.setItem(this.keyPrefix + athlete.id + this.upcomingActivitiesSuffix,serializedStorageItem);
      }
    }
  }

  /**
   * Updates the planned activity list in session storage for the current athlete
   * @param upcomingActivities a list of PlannedActivity to be written to ss
   */
  updateUpcomingActivitiesForAthlete(upcomingActivities:PlannedActivity[]) {
    const currentAthlete = this.getCurrentAthlete().id;
    const serializedStorageItem = JSON.stringify(upcomingActivities);
    sessionStorage.setItem(this.keyPrefix + currentAthlete + this.upcomingActivitiesSuffix,serializedStorageItem);
  }

  /**
   * Returns the upcoming activities of the currently "selected" athlete
   * if existent in sessions storage
   * else returns null
   */
  getUpcomingActivitiesForCurrentAthlete():PlannedActivity[] {
    const currentAthlete = this.getCurrentAthlete().id;
    const key = this.keyPrefix + currentAthlete + this.upcomingActivitiesSuffix;

    if (sessionStorage.getItem(key)) {
      return JSON.parse(sessionStorage.getItem(this.keyPrefix + currentAthlete + this.upcomingActivitiesSuffix));
    }
    return null;
  }

  /**
   * Fetches all template activities for the currently logged in trainer
   */
  fetchTemplateActivitiesForTrainer():Observable<PlannedActivity[]> {
    return this.httpClient.get<PlannedActivity[]>(this.templateActivitiesBaseUri,
      { params: { withCredentials: true } },
    );
  }

  /**
   *  Puts a list of PlannedActivity into the session storage
   *  under the key this.keyPrefix + "template_activities"
   * @param templateActivities a list of PlannedActivity to be written to ss
   */
  updateTemplateActivities(templateActivities:PlannedActivity[]) {
    const serializedStorageItem = JSON.stringify(templateActivities);
    sessionStorage.setItem(this.keyPrefix + "template_activities",serializedStorageItem);
  }

  /**
   * Returns the template activities of the currently logged in trainer
   * if existent in sessions storage
   * else returns null
   */
  getTemplateActivities():PlannedActivity[] {
    if (sessionStorage.getItem(this.keyPrefix + "template_activities")) {
      return JSON.parse(sessionStorage.getItem(this.keyPrefix + "template_activities"));
    }
    return null;
  }

  /**
   * Returns all upcoming activities for a given user id
   * @param id id of the athlete
   */
  getUpcomingActivityIdsForAthlete(id:number):PlannedActivity[] {
    if (sessionStorage.getItem(this.keyPrefix + id + this.upcomingActivitiesSuffix)) {
      return JSON.parse(sessionStorage.getItem(this.keyPrefix + id + this.upcomingActivitiesSuffix));
    }
    return null;
  }

  /**
   * Returns a "json-like" object used for the csp request
   * @param id id of the athlete
   */
  getUpcomingActivityIdsForAthleteInRequestFormat(id:number):any[] {
    const allUpcomingActivities: PlannedActivity[] = JSON.parse(sessionStorage.getItem(this.keyPrefix + id + this.upcomingActivitiesSuffix));
    const ret = [];
    for (const activity of allUpcomingActivities) {
      if (activity.template) {
        ret.push({
          "id": activity.id,
          "withTrainer": activity.withTrainer,
        });
      }
    }
    return ret;
  }

  /**
   * Validate if a list of planned activities breaks any hard constraints of the scheduler
   * @param user user to validate the planned activities for
   * @param activities list of planned activities to validate
   */
  validatePlannedActivities(user:User,activities:PlannedActivity[]) {
    if (activities.length > 7) {
      throw new Error("Too many activities planned for athlete " + user.name);
    }
    let highCnt = 0;
    let sum = 0;
    for (const a of activities) {
      if (a.load === Load.HARD) {
        highCnt++;
      }
      sum += convertLoadToInt(a.load);
    }
    if (highCnt > 4) {
      throw new Error("Too many HARD load activities planned for athlete " + user.name);
    }
    if (sum > 8) {
      throw new Error("Too much load in activities planned for athlete " + user.name);
    }

  }

  /**
   * Issues the scheduling request
   */
  sendCSPRequest():Observable<any> {
    const request:any = { "mappings": [] };
    for (const athlete of JSON.parse(sessionStorage.getItem(this.athletesKey))) {
      const tmp = this.getUpcomingActivityIdsForAthleteInRequestFormat(athlete.id);
      this.validatePlannedActivities(athlete,this.getUpcomingActivityIdsForAthlete(athlete.id));
      request["mappings"].push({
        "userId": athlete.id,
        "activities": tmp,
      });
    }
    return this.httpClient.post(this.cspScheduleUri,request,{ params: { withCredentials: true } });
  }

  /**
   * Fetches the job status from the Backend
   * (i.e.) if there is already a plan (being) created for next week
   */
  fetchJobExists():Observable<boolean> {
    return this.httpClient.get<boolean>(
      this.cspScheduleUri,
      { params: { withCredentials: true } },
    );
  }

  /**
   * Deletes the current trainingsplan for the active user(trainer) and all related athletes
   */
  sendJobDeleteRequest():Observable<any> {
    return this.httpClient.delete(this.cspScheduleUri,{ params: { withCredentials: true } });
  }

  /**
   * (Part of the current athlete - functionality)
   * This returns the "currently selected" athlete
   * or null if not set currently
   */
  getCurrentAthlete(): User {
    if (sessionStorage.getItem(this.athletesKey)) {
      return JSON.parse(sessionStorage.getItem(this.athletesKey))[this.currentIndex];
    }
    return null
  }

  /**
   * (Part of the current athlete - functionality)
   * This "selects" the next athlete in the list
   */
  nextAthlete(): void {
    this.currentIndex = (this.currentIndex + 1) % JSON.parse(sessionStorage.getItem(this.athletesKey)).length;
  }

  /**
   * (Part of the current athlete - functionality)
   * This "selects" the previous athlete in the list
   */
  prevAthlete(): void {
    this.currentIndex = (this.currentIndex - 1 + JSON.parse(sessionStorage.getItem(this.athletesKey)).length) % JSON.parse(sessionStorage.getItem(this.athletesKey)).length;
  }
}
