import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Interval } from "../../dto/Interval";
import { Step, StepDurationUnit, StepDurationType, StepTargetType, StepType } from "../../dto/Step";
import { ActivityType } from "../../../../domain/activity/dto/PlannedActivity";

@Component({
  selector: 'app-interval-container',
  templateUrl: './interval-container.component.html',
  styleUrls: ['./interval-container.component.scss'],
})
export class IntervalContainerComponent implements OnInit {

  @Input() editable = false;
  @Input() activityType: ActivityType;
  @Input() maxNesting = 2;
  @Input() inputInterval: Interval;
  @Output() changeInterval: EventEmitter<Interval> = new EventEmitter();

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
          id: null,
          type: StepType.WARMUP,
          durationType: StepDurationType.LAPBUTTON,
        },
      },
      {
        repeat: 2,
        steps: [
          {
            repeat: 1,
            steps: {
              id: null,
              type: StepType.ACTIVE,
              durationType: StepDurationType.DISTANCE,
              duration: 1,
              durationUnit: StepDurationUnit.KM,
              targetType: StepTargetType.CADENCE,
              targetFrom: 170,
              targetTo: 175,
              note: "example note",
            },
          },
          {
            repeat: 1,
            steps: {
              id: null,
              type: StepType.RECOVERY,
              durationType: StepDurationType.LAPBUTTON,
            },
          },
        ],
      },
      {
        repeat: 1,
        steps: {
          id: null,
          type: StepType.COOLDOWN,
          durationType: StepDurationType.LAPBUTTON,
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
    // emit the default or given interval right away
    this.changeInterval.emit(this.interval);
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
    const i: Interval = { id: null, repeat: 1, steps: {
      id: null,
      type: StepType.ACTIVE,
      durationType: StepDurationType.LAPBUTTON,
    } }
    this.addInterval(i);
  }
}
