import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Interval } from "../../dto/Interval";
import { Step, StepDurationDistanceUnit, StepDurationType, StepTargetType, StepType } from "../../dto/Step";

@Component({
  selector: 'app-interval-container',
  templateUrl: './interval-container.component.html',
  styleUrls: ['./interval-container.component.scss'],
})
export class IntervalContainerComponent implements OnInit {

  // @Input() editable: boolean;
  // @Input() activityType: string; // TODO should be some Enum
  @Input() inputInterval: Interval;
  @Output() changeInterval: EventEmitter<Interval> = new EventEmitter();

  // TODO replace these by @Input statements
  editable = true;
  activityType: StepType = StepType.RUN;
  maxNesting = 2;

  // local state
  currentId = 0;
  allIds: number[] = [];
  intervalIDs: number[] = [];

  // standard interval
  interval: Interval = {
    repeat: 1,
    steps: [
      {
        repeat: 1,
        steps: {
          type: StepType.WARMUP,
          duration_type: StepDurationType.LAPBUTTON,
        },
      },
      {
        repeat: 2,
        steps: [
          {
            repeat: 1,
            steps: {
              type: this.activityType,
              duration_type: StepDurationType.DISTANCE,
              duration_distance: 1,
              duration_distance_unit: StepDurationDistanceUnit.KM,
              target_type: StepTargetType.CADENCE,
              target_from: 170,
              target_to: 175,
              note: "example note",
            },
          },
          {
            repeat: 1,
            steps: {
              type: StepType.RECOVERY,
              duration_type: StepDurationType.LAPBUTTON,
            },
          },
        ],
      },
      {
        repeat: 1,
        steps: {
          type: StepType.COOLDOWN,
          duration_type: StepDurationType.LAPBUTTON,
        },
      },
    ],
  }

  // everytime the interval changes, emit the new interval
  onIntervalUpdated() {
    this.changeInterval.emit(this.interval);
  }

  handleDeleteInterval(id: number) {
    this.deleteIntervalWithId(id, this.interval);
    this.interval = Object.assign({}, this.interval);
    this.onIntervalUpdated();
  }

  handleChangeInterval(newInterval: Interval) {
    this.interval = Object.assign({}, newInterval);
    this.onIntervalUpdated()
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
    // if there is an input interval, replace the placeholder interval
    if (this.inputInterval) {
      this.interval = Object.assign({}, this.inputInterval);
    }
    this.addIdToInterval(this.interval);
  }


  addInterval(newInterval?: Interval) {

    if (!newInterval) {
      newInterval = { repeat: 2, steps: [] }
    }
    // if this interval already has an array of children, push to it
    if (Array.isArray(this.interval.steps)) {
      this.addIdToInterval(newInterval);
      this.interval.steps.push(newInterval)
      // to trigger change detection
      this.interval = Object.assign({}, this.interval);
    } else {
      // if interval.steps is a single step element, first replace it by an interval, then push to this interval
      const tempStep: Step = this.interval.steps;
      // the new root interval contains the old step and the newly added interval
      const newRootInterval: Interval = {
        repeat: 1,
        steps: [
          {
            repeat: 1,
            steps: tempStep,
          },
          newInterval,
        ],
      }
      this.addIdToInterval(newRootInterval);
      this.interval = Object.assign({}, newRootInterval);
    }
    this.onIntervalUpdated();
  }

  addStep() {
    // new step to be added
    const i: Interval = { repeat: 1, steps: {
      type: this.activityType,
      duration_type: StepDurationType.LAPBUTTON,
    } }
    this.addInterval(i);
  }

  logInterval() {
    console.log(this.interval)
  }

}
