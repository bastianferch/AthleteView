import { Component, OnInit } from '@angular/core';
import { Interval } from "../../dto/Interval";

@Component({
  selector: 'app-interval-container',
  templateUrl: './interval-container.component.html',
  styleUrls: ['./interval-container.component.scss'],
})
export class IntervalContainerComponent implements OnInit {

  // @Input() editable: boolean;
  // @Input() activityType: string; // TODO should be some Enum
  // @Input() interval: any; // TODO should have a type. Wait for Basti to declare type for this.
  // @Output() changeInterval: EventEmitter<any> = new EventEmitter();

  // TODO replace these by @Input statements
  editable = true;
  activityType = "running";

  currentId = 0;
  allIds: number[] = [];

  // TODO test intervals
  interval: Interval = {
    repeat: 1,
    steps: [
      {
        repeat: 1,
        steps: {
          type: "warm up",
          duration_type: "lap button press",
          note: "warm up note",
        },
      },
      {
        repeat: 2,
        steps: [
          {
            repeat: 1,
            steps: {
              type: "run",
              duration_type: "distance",
              duration_distance: 1,
              duration_distance_unit: "km",
              target_type: "cadence",
              target_from: 170,
              target_to: 175,
              note: "run note",
            },
          },
          {
            repeat: 1,
            steps: {
              type: "recovery",
              duration_type: "lap button press",
              note: "recovery note",
            },
          },
        ],
      },
      {
        repeat: 1,
        steps: {
          type: "cool down",
          duration_type: "lap button press",
          note: "cool down note",
        },
      },
    ],
  }

  newId() {
    const result = this.currentId;
    this.currentId++;
    this.allIds.push(result);
    return result;
  }

  addIdToInterval(i: Interval) {
    if (Array.isArray(i.steps)) {
      i.id = this.newId();
      for (const subI of i.steps) {
        this.addIdToInterval(subI);
      }
    }
  }

  ngOnInit() {
    this.addIdToInterval(this.interval);
  }


  addInterval() {
    // eslint-disable-next-line no-console
    console.log("add interval");
    // TODO if interval.steps is a single step element, first replace it by an interval, then push to this interval
    if (Array.isArray(this.interval.steps)) {
      this.interval.steps.push({ repeat: 2, steps: [], id: this.newId() })
      // to trigger change detection
      this.interval = Object.assign({}, this.interval);
    }
  }

  addStep() {
    // eslint-disable-next-line no-console
    console.log("add step")
    // TODO if interval.steps is a single step element, first replace it by an interval, then push to this interval
    if (Array.isArray(this.interval.steps)) {
      this.interval.steps.push({ repeat: 1, steps: {
        type: "run",
        duration_type: "distance",
        duration_distance: 1,
        duration_distance_unit: "km",
        target_type: "cadence",
        target_from: 170,
        target_to: 175,
        note: "run note",
      } })
      // to trigger change detection
      this.interval = Object.assign({}, this.interval);
    }
  }

}
