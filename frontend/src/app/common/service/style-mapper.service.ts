import { Injectable } from '@angular/core';
import { ActivityType, Load, PlannedActivity } from "../../domain/activity/dto/PlannedActivity";
import { Activity } from 'src/app/domain/activity/dto/Activity';

@Injectable({
  providedIn: 'root',
})
export class StyleMapperService {

  getIntensityColor(activity: PlannedActivity): string {
    const colorMapping: { [key in Load]: string } = {
      [Load.LOW]: '#82e010',
      [Load.MEDIUM]: '#f0d807',
      [Load.HARD]: '#de2b0b',
    };
    return colorMapping[activity.load]
  }

  getIconPathForActivity(activity: Activity): string {
    return this.getIconPathForActivityType(activity.activityType)
  }

  getIconPathForPlannedActivity(activity: PlannedActivity): string {
    return this.getIconPathForActivityType(activity.type)
  }

  private getIconPathForActivityType(type: ActivityType): string {
    if (!(type in ActivityType)) {
      return 'assets/placeholder.png'
    }
    const iconMapping: { [key in ActivityType]: string } = {
      [ActivityType.SWIM]: 'swim_icon.png',
      [ActivityType.RUN]: 'run_icon.png',
      [ActivityType.BIKE]: 'bike_icon.png',
      [ActivityType.ROW]: 'row_icon.png',
      [ActivityType.CROSSCOUNTRYSKIING]: 'crosscountryskiing_icon.png',
    };
    return 'assets/activityIcons/' + iconMapping[type]
  }
}
