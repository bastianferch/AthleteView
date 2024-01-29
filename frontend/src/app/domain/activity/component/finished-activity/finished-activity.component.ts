import { ChangeDetectorRef, Component, HostListener, OnInit, ViewChild, ViewEncapsulation } from '@angular/core'; // , ViewChild
import { Activity } from '../../dto/Activity';
import { ActivityService } from '../../service/activity.service';
import { ActivatedRoute } from '@angular/router';
import { MatTableDataSource } from '@angular/material/table';
import { Lap } from 'src/app/common/interval/dto/Lap';
import { format, formatDuration, intervalToDuration } from 'date-fns';
import { ActivityType, PlannedActivitySplit } from '../../dto/PlannedActivity';
import { MapDataDto } from '../../dto/MapDataDto';

import * as L from 'leaflet';
import { MatPaginator } from '@angular/material/paginator';
import { MobileCheckService } from 'src/app/common/service/mobile-checker.service';
import { StyleMapperService } from 'src/app/common/service/style-mapper.service';
import { Interval, convertToInterval } from 'src/app/common/interval/dto/Interval'
import { AuthService } from "../../../auth/service/auth.service";

@Component({
  selector: 'app-finished-activity',
  templateUrl: './finished-activity.component.html',
  styleUrls: ['./finished-activity.component.scss'],
  encapsulation: ViewEncapsulation.None,
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
  coordinatesReady = false
  ds = new MatTableDataSource<Lap>()
  options: L.MapOptions = { zoom: 13 }

  idEmitter: number
  activityType: ActivityType
  activityDate: Date
  hasPlannedActivityAssigned = false
  isTrainer = false

  // used for paginator
  @ViewChild(MatPaginator, { static: false })
  set paginator(v: MatPaginator) {
    this.ds.paginator = v
  }

  protected activity: Activity
  private polylineRoute: L.Polyline

  constructor(
    private activityService: ActivityService,
    private authService: AuthService,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef,
    private mobileCheck: MobileCheckService,
    private styleMapper: StyleMapperService,
  ) { }

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      this.isLoading = true
      let loadCnt = 0
      const aid = params["id"]
      this.idEmitter = aid

      this.activityService.fetchMapDataForActivity(aid).subscribe((data: Array<MapDataDto>) => {
        this.options = {
          layers: this.getLayers(data),
        }
        loadCnt += 1

        if (loadCnt >= 2) {
          this.isLoading = false
          this.cdr.detectChanges()
        }
      })

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

        if (data.plannedActivity !== undefined && data.plannedActivity !== null) {
          this.hasPlannedActivityAssigned = true
        }

        this.ds.data = data.laps
        loadCnt += 1
        if (loadCnt >= 2) {
          this.isLoading = false
          this.cdr.detectChanges()
        }
        this.activityType = data.activityType
        this.activityDate = data.startTime as Date
        this.activity = data
      })
    })
    this.isTrainer = !this.authService.currentUser.isAthlete()
  }

  onMapReady(map: L.Map) {
    map.fitBounds(this.polylineRoute.getBounds(), {
      padding: L.point(24, 24),
      maxZoom: 13,
      animate: true,
    })

    // necessary or else map-tiles don't load properly
    setTimeout(() => {
      map.invalidateSize()
    }, 0)
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

  kcalNotNull(): boolean {
    return this.activity.spentKcal !== 0
  }

  getTooltipText(): string {
    return `
    Series in the graph can be hidden.
    Do so by clicking on the colors/names below the graph.
    Cadence units: spm (strides/strokes per min), rpm (rotations per min).
    `
  }

  getIconPath(): string {
    return this.styleMapper.getIconPathForActivity(this.activity)
  }

  getMappedInterval(): Interval {
    return convertToInterval((this.activity.plannedActivity as any as PlannedActivitySplit).interval)
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

  private getLayers(routeData: Array<MapDataDto>): L.Layer[] {
    if (routeData.length > 0) {
      this.coordinatesReady = true
    }

    this.polylineRoute = this.getRoute(routeData)
    return [
      new L.TileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; OpenStreetMap contributors',
      } as L.TileLayerOptions),
      this.polylineRoute,
    ] as L.Layer[];
  }

  private getRoute(routeData: Array<MapDataDto>): L.Polyline {
    return L.polyline(
      routeData.map((it: MapDataDto): L.LatLng => new L.LatLng(it.latitude, it.longitude)) as L.LatLng[],
      {
        color: "#9e30ff",
      } as L.PolylineOptions,
    )
  }

  @HostListener("window:resize")
  private handleResize() {
    if (this.mobileCheck.isMobile(900)) {
      if (this.mobileCheck.isMobile(600)) {
        this.columnsToDisplay = [
          "cumulativeTime",
          "distanceColumn",
          "avgSpeed",
          "avgBpm",
        ]
      } else {
        this.columnsToDisplay = [
          "cumulativeTime",
          "distanceColumn",
          "avgSpeed",
          "avgPower",
          "avgBpm",
          "avgCadence",
        ]
      }
    } else {
      this.columnsToDisplay = [
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
    }

    this.cdr.detectChanges()
  }
}
