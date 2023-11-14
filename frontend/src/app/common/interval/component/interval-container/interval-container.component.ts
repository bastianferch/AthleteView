import { Component, OnInit } from '@angular/core';
import { Interval } from "../../dto/Interval";
import { StepDurationDistanceUnit, StepDurationType, StepTargetType, StepType } from "../../dto/Step";

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
  intervalIDs: number[] = [];

  // TODO test intervals
  interval: Interval = {
    repeat: 1,
    steps: [
      {
        repeat: 1,
        steps: {
          type: StepType.WARMUP,
          duration_type: StepDurationType.LAPBUTTON,
          note: "warm up note",
        },
      },
      {
        repeat: 2,
        steps: [
          {
            repeat: 1,
            steps: {
              type: StepType.RUN,
              duration_type: StepDurationType.DISTANCE,
              duration_distance: 1,
              duration_distance_unit: StepDurationDistanceUnit.KM,
              target_type: StepTargetType.CADENCE,
              target_from: 170,
              target_to: 175,
              note: "run note",
            },
          },
          {
            repeat: 1,
            steps: {
              type: StepType.RECOVERY,
              duration_type: StepDurationType.LAPBUTTON,
              note: "recovery note",
            },
          },
        ],
      },
      {
        repeat: 1,
        steps: {
          type: StepType.COOLDOWN,
          duration_type: StepDurationType.LAPBUTTON,
          note: "cool down note",
        },
      },
    ],
  }

  handleDeleteInterval(id: number) {
    this.deleteIntervalWithId(id, this.interval);
    this.interval = Object.assign({}, this.interval);
  }

  handleChangeInterval(newInterval: Interval) {
    this.interval = Object.assign({}, newInterval);
  }

  deleteIntervalWithId(id: number, i: Interval) {
    if (Array.isArray(i.steps)) {
      for (const step of i.steps) {
        if (step.id === id) {
          // if this is the right interval, delete all sub-intervals first
          if (Array.isArray(step.steps)) {
            for (const s of step.steps) {
              this.deleteIntervalWithId(s.id, step);
            }
          }
          // now remove step from this interval
          i.steps = i.steps.filter((interv) => interv.id !== id);
          this.allIds = this.allIds.filter((j) => j !== id);
          this.intervalIDs = this.intervalIDs.filter((j) => j !== id);
          return;
        }
        this.deleteIntervalWithId(id, step);
      }
    }
  }

  newId(i: Interval) {
    const result = this.currentId;
    this.allIds.push(result);
    this.currentId++;

    // if i is an interval with other intervals as children
    if (Array.isArray(i?.steps)) {
      this.intervalIDs.push(result);
    }

    return result;
  }

  addIdToInterval(i: Interval) {
    i.id = this.newId(i);
    if (Array.isArray(i.steps)) {
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
    // TODO if interval.steps is a single step element, first replace it by an interval, then push to this interval
    if (Array.isArray(this.interval.steps)) {
      const i: Interval = { repeat: 2, steps: [] }
      this.addIdToInterval(i);
      this.interval.steps.push(i)
      // to trigger change detection
      this.interval = Object.assign({}, this.interval);
    }
  }

  addStep() {
    // eslint-disable-next-line no-console
    // TODO if interval.steps is a single step element, first replace it by an interval, then push to this interval
    if (Array.isArray(this.interval.steps)) {
      const i: Interval = { repeat: 1, steps: {
        type: StepType.RUN,
        duration_type: StepDurationType.DISTANCE,
        duration_distance: 1,
        duration_distance_unit: StepDurationDistanceUnit.KM,
        target_type: StepTargetType.CADENCE,
        target_from: 170,
        target_to: 175,
        note: "run note",
      } }
      this.addIdToInterval(i);
      this.interval.steps.push(i)
      // to trigger change detection
      this.interval = Object.assign({}, this.interval);
    }
  }

  logInterval() {
    console.log(this.interval)
  }

}
