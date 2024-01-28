import { Component, HostListener, Input, OnChanges, SimpleChange } from '@angular/core';
import * as hc from 'highcharts';
import NoDataToDisplay from 'highcharts/modules/no-data-to-display';
import { ActivityService } from '../../../service/activity.service';
import { ActivityStatsDto } from '../../../dto/ActivityStatsDto';
import { SpeedMapper } from 'src/app/common/util/parsing/speed-mapper';
import { ActivityType } from '../../../dto/PlannedActivity';
import { formatDuration, intervalToDuration } from 'date-fns';
import { MobileCheckService } from 'src/app/common/service/mobile-checker.service';


const BPM_GRAPH_COLOR = `rgba(255, 64, 79, 1)`
const SPD_GRAPH_COLOR = "#9E30FF"
const CAD_GRAPH_COLOR = "#059bff"

const BACKGROUND_AREA_COLOR = `rgba(169, 169, 169, 0.6)`
const LIGHT_TEXT_FALLBACK = "#000000"


@Component({
  selector: 'app-activity-graph',
  templateUrl: './activity-graph.component.html',
  styleUrls: ['./activity-graph.component.scss'],
})
export class ActivityGraphComponent implements OnChanges {
  @Input({ required: true }) currentId!: number
  @Input({ required: true }) activityType!: ActivityType
  @Input({ required: true }) activityDate!: Date

  private bpmOpts: hc.Options
  private spdOpts: hc.Options
  private isMobile = false
  private statsData: Array<ActivityStatsDto> = []


  Highcharts: typeof hc = hc
  chart: any // Highcharts.Chart
  chartView = "bpm"

  hc_opts: hc.Options = {}

  isLoading = true
  graphLoaded = false

  constructor(
    private activityService: ActivityService,
    private speedMapper: SpeedMapper,
    private mobileCheck: MobileCheckService,
  ) {
    NoDataToDisplay(hc)
  }

  chartCallback: Highcharts.ChartCallbackFunction = (chart) => {
    this.chart = chart;
    this.handleResize()
    this.chart.reflow()
  }

  ngOnChanges(ch: any): void {
    const sc = ch["currentId"] as SimpleChange
    if (sc.firstChange || sc.currentValue !== sc.previousValue) {
      this.loadData(sc.currentValue)
    }
  }

  private loadData(aid: number) {
    this.activityService.fetchGraphDataForActivity(aid).subscribe((data: Array<ActivityStatsDto>) => {
      this.statsData = data
      this.configureGraph(data)
      this.graphLoaded = true
      if (this.chart !== undefined) {
        this.chart.reflow()
      }
    })
  }

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

  private configureGraph(data: Array<ActivityStatsDto>) {
    const speedUnit = this.speedMapper.formatSpeedByTypeWithUnit(0, this.activityType).split(" ")[1]
    const seriesConfig = this.getSeriesConfiguration(data, speedUnit)

    this.hc_opts = {
      title: {
        text: "Visualized Activity",
      },
      yAxis: seriesConfig[1],
      xAxis: {
        type: "datetime",
        labels: {
          formatter: (value) => {
            return this.formatDuration(intervalToDuration({ start: this.activityDate, end: new Date(value.value) }))
          },
        },
        alignTicks: false,
      },
      tooltip: {
        shared: true,
      },
      series: seriesConfig[0],
      chart: {
        zooming: {
          type: "xy",
        },
      },
      plotOptions: {
        series: {
          pointStart: this.activityDate.getTime(),
          events: {
            legendItemClick: function () {
              const seriesidx = this.index
              const yaxis = this.chart.yAxis[seriesidx]

              yaxis.update({
                visible: !yaxis.options.visible,
              })
              this.chart.redraw()
              return true
            },
          },
        },
      },
      lang: {
        noData: "No data to display",
      },
      time: {
        timezoneOffset: this.activityDate.getTimezoneOffset(),
      },
    }
  }

  private getSeriesConfiguration(data: Array<ActivityStatsDto>, speedUnit: string): [Array<hc.SeriesOptionsType>, Array<hc.YAxisOptions>] { // Array<hc.SeriesOptionsType> {
    const series: Array<hc.SeriesOptionsType> = []
    const axis: Array<hc.YAxisOptions> = []

    const bpmData = data.map((elem) => [(elem.timestamp as Date).getTime(), elem.bpm])
    const spdData = data.map((elem) => [(elem.timestamp as Date).getTime(), elem.speed])
    const powData = data.map((elem) => [(elem.timestamp as Date).getTime(), elem.power])
    const cadData = data.map((elem) => [(elem.timestamp as Date).getTime(), elem.cadence])
    const altData = data.map((elem) => [(elem.timestamp as Date).getTime(), elem.altitude])

    let altitudePresent = false

    if (altData.length > 0) {
      altitudePresent = true

      const altConf: hc.SeriesOptionsType = {
        type: "areaspline",
        data: altData,
        name: "Altitude",
        color: BACKGROUND_AREA_COLOR,
        yAxis: axis.length,
      }

      axis.push(this.getAltitudeAxisConf())
      series.push(altConf)
    }

    if (bpmData.length > 0) {
      const bpmConf: hc.SeriesOptionsType = {
        type: altitudePresent ? "spline" : "areaspline",
        data: bpmData,
        name: "BPM",
        color: BPM_GRAPH_COLOR,
        yAxis: axis.length,
      }

      axis.push(this.getBpmAxisConf())
      series.push(bpmConf)
    }

    if (spdData.length > 0) {
      const spdConf: hc.SeriesOptionsType = {
        type: "spline",
        data: spdData,
        name: speedUnit,
        color: SPD_GRAPH_COLOR,
        yAxis: axis.length,
      }
      axis.push(this.getSpdAxisConf(speedUnit))
      series.push(spdConf)
    }

    if (powData.length > 0) {
      const powConf: hc.SeriesOptionsType = {
        type: "spline",
        data: powData,
        name: "Watt",
        color: LIGHT_TEXT_FALLBACK,
        visible: false,
        yAxis: axis.length,
      }
      axis.push(this.getPowerAxisConf())
      series.push(powConf)
    }

    if (cadData.length > 0) {
      const cadConf: hc.SeriesOptionsType = {
        type: "spline",
        data: cadData,
        name: this.getCadenceForType(),
        color: CAD_GRAPH_COLOR,
        yAxis: axis.length,
      }
      axis.push(this.getCadenceAxisConf())
      series.push(cadConf)
    }

    // switch half of axes to opposite side
    if (axis.length > 1) {
      for (let toIdx = Math.floor(axis.length / 2) - 1; toIdx >= 0; --toIdx) {
        axis[toIdx]["opposite"] = true
      }
    }

    return [series, axis]
  }

  private getBpmAxisConf(): hc.YAxisOptions {
    return {
      labels: {
        format: "{value}",
        style: {
          color: BPM_GRAPH_COLOR,
        },
      },
      title: {
        text: this.isMobile ? "" : "Heartfrequency [BPM]",
        style: {
          color: BPM_GRAPH_COLOR,
        },
      },
    }
  }


  private getSpdAxisConf(speedUnit: string): hc.YAxisOptions {
    return {
      labels: {
        format: "{value}",
        style: {
          color: SPD_GRAPH_COLOR,
        },
      },
      title: {
        text: this.isMobile ? "" : `Speed [${speedUnit}]`,
        style: {
          color: SPD_GRAPH_COLOR,
        },
      },
    }
  }

  private getAltitudeAxisConf(): hc.YAxisOptions {
    return {
      labels: {
        format: "{value}",
        style: {
          color: BACKGROUND_AREA_COLOR,
        },
      },
      title: {
        text: this.isMobile ? "" : "Altitude [m]",
        style: {
          color: BACKGROUND_AREA_COLOR,
        },
      },
    }
  }

  private getPowerAxisConf(): hc.YAxisOptions {
    return {
      labels: {
        format: "{value}",
        style: {
          color: LIGHT_TEXT_FALLBACK,
        },
      },
      title: {
        text: this.isMobile ? "" : "Power [Watt]",
        style: {
          color: LIGHT_TEXT_FALLBACK,
        },
      },
      visible: false,
    }
  }


  private getCadenceAxisConf(): hc.YAxisOptions {
    return {
      labels: {
        format: "{value}",
        style: {
          color: CAD_GRAPH_COLOR,
        },
      },
      title: {
        text: this.isMobile ? "" : `Cadence [${this.getCadenceForType()}]`,
        style: {
          color: CAD_GRAPH_COLOR,
        },
      },
    }
  }


  private mapAndSmoothSpeed(speed: Array<number>): Array<number> {
    const mapped = speed.map((spd: number) => Number(this.speedMapper.formatSpeedByType(spd, this.activityType)))
    // smoothen values
    for (let i = 0; i < mapped.length; ++i) {
      if (!Number.isFinite(mapped[i])) {
        if (i === 0) {
          // can't set it to previous
          mapped[i] = 0.0
        } else {
          mapped[i] = mapped[i - 1]
        }
      }
    }
    return mapped
  }


  private getCadenceForType(): string {
    switch (this.activityType) {
      case ActivityType.BIKE:
        return "rpm"
      case ActivityType.ROW:
        return "spm"
      case ActivityType.RUN:
        return "spm"
      case ActivityType.SWIM:
        return "spm"
      default:
        return "spm"
    }
  }

  @HostListener("window:resize")
  private handleResize() {
    this.isMobile = this.mobileCheck.isMobile()
    if (this.chart !== undefined) {
      this.configureGraph(this.statsData)
    }
  }
}
