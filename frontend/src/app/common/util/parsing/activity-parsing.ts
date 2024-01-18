import { Injectable } from "@angular/core";
import { ActivityType } from "../../../domain/activity/dto/PlannedActivity";

@Injectable({
  providedIn: 'root',
})
export class ActivityParsing {

  getReadableNameForActivity(activityType: string): string {
    switch (activityType) {
      case ActivityType.BIKE:
        return "Cycling"
      case ActivityType.CROSSCOUNTRYSKIING:
        return "Cross-country Skiing"
      case ActivityType.ROW:
        return "Rowing"
      case ActivityType.RUN:
        return "Running"
      case ActivityType.SWIM:
        return "Swimming"
      default:
        // shouldn't occur
        return "Other"
    }
  }

  getIconNameForActivity(activityType: string): string {
    switch (activityType) {
      case ActivityType.BIKE:
        return "directions_bike"
      case ActivityType.CROSSCOUNTRYSKIING:
        return "downhill_skiing"
      case ActivityType.ROW:
        return "rowing"
      case ActivityType.RUN:
        return "directions_run"
      case ActivityType.SWIM:
        return "pool"
      default:
        // shouldn't occur
        return "sync-problem"
    }
  }

}
