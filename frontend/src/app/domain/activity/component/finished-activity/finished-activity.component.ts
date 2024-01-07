import { ChangeDetectorRef, Component, OnInit } from '@angular/core'; // , ViewChild
import { Activity } from '../../dto/Activity';
import { ActivityService } from '../../service/activity.service';
import { ActivatedRoute } from '@angular/router';
// import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { Lap } from 'src/app/common/interval/dto/Lap';
import { format, formatDuration, intervalToDuration } from 'date-fns';
import { ActivityType } from '../../dto/PlannedActivity';

@Component({
  selector: 'app-finished-activity',
  templateUrl: './finished-activity.component.html',
  styleUrls: ['./finished-activity.component.scss'],
})
export class FinishedActivityComponent implements OnInit {
  columnsToDisplay = [
    "lapCount",
    "distanceColumn",
    "timeColumn",
    "cumulativeTime",
    "avgSpeed",
    "avgPower",
    "maxPower",
    "avgBpm",
    "maxBpm",
    "avgCadence",
    "maxCadence",
  ]

  isLoading = true
  ds = new MatTableDataSource<Lap>()

  // used for paginator
  // @ViewChild(MatPaginator, { static: false })
  // set paginator(v: MatPaginator) {
  //   this.ds.paginator = v
  // }

  protected activity: Activity

  constructor(
    private activityService: ActivityService,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      this.isLoading = true
      const aid = params["id"]
      this.activityService.fetchActivityForUser(aid).subscribe((data) => {
        data.laps = data.laps.sort((x: Lap, y: Lap) => {
          let order = 0
          if (x.id > y.id) {
            order = 1
          } else {
            order = -1
          }
          return order
        })
        let cumulativeTime = 0
        // calculate cumulative time over laps
        data.laps = data.laps.map((item) => {
          cumulativeTime += item.time
          return { ...item, cumulativeTime }
        })
        this.ds.data = data.laps
        this.isLoading = false
        this.activity = data
      })
    })
  }

  formatTime(time: number): string {
    const duration = intervalToDuration({ start: 0, end: time * 1000 })
    return this.formatDuration(duration)
  }

  formatDistance(distance: number): string {
    return (distance / 1000).toFixed(2)
  }

  formatSpeedByType(spd: number): string {
    let normalized_spd: number

    // convert m/s to...
    switch (this.activity.activityType) {
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
    return String(normalized_spd.toFixed(2))
  }

  getSpeedUnitByType(): string {
    switch (this.activity.activityType) {
      case ActivityType.RUN:
        return "min/km"
      case ActivityType.ROW:
        return "min/500m"
      case ActivityType.SWIM:
        return "min/100m"
      default:
        return "km/h"
    }
  }

  getHeading(): string {
    let str = "Activity"
    if (this.activity.plannedActivity !== null && this.activity.plannedActivity.name !== null) {
      str += " (" + this.activity.plannedActivity.name + ")"
    }
    str += " on "
    str += format(new Date(this.activity.startTime as Date), "dd.MM.yyyy")
    return str
  }

  getIconForActivity(): string {
    switch (this.activity.activityType) {
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

  getReadableNameForActivity(): string {
    switch (this.activity.activityType) {
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
        return ""
    }
  }

  activityHasDate(): boolean {
    return this.activity.startTime !== null || this.activity.endTime !== null
  }

  getFormattedDateForActivity(): string {
    let dateStr = ""

    if (this.activity.startTime !== null) {
      dateStr += format(new Date(this.activity.startTime as Date), "iiii, LLL do yyyy, HH:mm")
    }

    if (this.activity.endTime !== null) {
      if (dateStr !== "") {
        // startTime was present, short-format is enough
        dateStr += " - "
        dateStr += format(new Date(this.activity.endTime as Date), "HH:mm")
      } else {
        dateStr += format(new Date(this.activity.endTime as Date), "iiii, LLL do yyyy, HH:mm")
      }
    }

    return dateStr
  }

  getTotalTime(): string {
    const duration = intervalToDuration({ start: this.activity.startTime as Date, end: this.activity.endTime as Date })
    return this.formatDuration(duration)
  }

  getTotalDistance(): string {
    return `${(this.activity.distance / 1000).toFixed(2)} km`
  }

  getSpentKcal(): string {
    return String(this.activity.spentKcal.toFixed(2))
  }

  getBpmAverage(): string {
    return `${this.activity.averageBpm} bpm`
  }

  getMaxBpm(): string {
    return String(this.activity.maxBpm)
  }

  getMinBpm(): string {
    return String(this.activity.minBpm)
  }

  // https://stackoverflow.com/a/65711327
  private formatDuration(duration: Duration): string {
    const zeroPad = (it: number) => String(it).padStart(2, '0')

    let formatted = formatDuration(duration, {
      format: ["hours", "minutes", "seconds"],
      zero: true,
      delimiter: ":",
      locale: {
        formatDistance: (_token, count) => zeroPad(count),
      },
    })

    if (formatted.startsWith("00:")) {
      // remove empty hours-segment
      const fsplit = formatted.split(":")
      fsplit.shift()
      formatted = fsplit.join(":")
    }
    return formatted
  }
}
