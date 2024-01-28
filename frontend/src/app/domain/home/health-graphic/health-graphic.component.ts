import { AfterViewInit, Component, Input, OnChanges } from '@angular/core';
import * as Highcharts from 'highcharts';

@Component({
  selector: 'app-health-graphic',
  templateUrl: './health-graphic.component.html',
  styleUrls: ['./health-graphic.component.scss'],
})
export class HealthGraphicComponent implements OnChanges, AfterViewInit {
  @Input() data: number[]
  Highcharts: typeof Highcharts = Highcharts;
  chartOptions: Highcharts.Options;

  ngOnChanges(): void {
    this.chartOptions = {
      chart: {
        type: 'line',
      },
      title: {
        text: 'Fitness (Health + Activity)',
      },
      xAxis: {
        title: {
          text: 'Weeks ago',
        },
        labels: {
          formatter: (value) => {
            return String(Math.abs(value.value as number))
          },
        },
      },
      yAxis: {
        title: {
          text: 'Fitness',
        },
      },
      series: [
        {
          data: this.data.map((value, index) => [index - 4, value]),
          type: 'line',
          name: 'Fitness',
        },
      ],
    };
  }

  ngAfterViewInit(): void {
    this.ngOnChanges()
  }
}
