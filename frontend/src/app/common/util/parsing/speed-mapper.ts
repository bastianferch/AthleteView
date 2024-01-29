import { Injectable } from "@angular/core";
import { ActivityType } from "src/app/domain/activity/dto/PlannedActivity";

@Injectable({
  providedIn: 'root',
})
export class SpeedMapper {
  formatSpeedByTypeWithUnit(speed: number, type: ActivityType): string {
    const spd = this.formatSpeedByType(speed, type)
    let unit = ""

    switch (type) {
      case ActivityType.RUN:
        unit = "min/km"
        break
      case ActivityType.ROW:
        unit = "min/500m"
        break
      case ActivityType.SWIM:
        unit = "min/100m"
        break
      default:
        unit = "km/h"
        break
    }

    return [spd, unit].join(" ")
  }

  formatSpeedByType(spd: number, type: ActivityType): string {
    let normalized_spd: number

    // convert m/s to...
    switch (type) {
      case ActivityType.RUN:
        // ... min/km
        normalized_spd = 1000 / 60 / spd
        break
      case ActivityType.ROW:
        // ... min/500m
        normalized_spd = spd / 500 / 60
        break
      case ActivityType.SWIM:
        // ... min/100m
        normalized_spd = spd / 100 / 60
        break
      default:
        // ... km/h
        normalized_spd = spd * 3.6
        break
    }

    return normalized_spd.toFixed(2)
  }
}
