import {
  ChangeDetectionStrategy,
  Component,
  Input,
  OnChanges,
  OnInit,
} from '@angular/core';
import { Step } from "../../dto/Step";
import { Interval } from "../../dto/Interval";
import { CdkDragDrop, moveItemInArray, transferArrayItem } from "@angular/cdk/drag-drop";

@Component({
  selector: 'app-interval',
  templateUrl: './interval.component.html',
  styleUrls: ['./interval.component.scss'],
  changeDetection: ChangeDetectionStrategy.Default,
})
export class IntervalComponent implements OnInit, OnChanges {
  @Input() editable: boolean;
  @Input() activityType: string; // TODO should be some Enum
  @Input() interval: Interval; // TODO
  @Input() allIDs: number[];
  // @Output() changeInterval: EventEmitter<any> = new EventEmitter(); // TODO

  // keeps track of the IDs of the other (nested) drag&drop lists, so they can be connected.
  allDragNDropIDs: string[] = [];

  ngOnInit(): void {
    this.resetIdArray();
  }

  // when the inputs change, update the ID array and update the interval so the sub-intervals are also notified about the change.
  ngOnChanges() {
    if (Array.isArray(this.interval.steps)) {
      // this is necessary to trigger change detection in the sub-intervals
      this.interval.steps = this.interval.steps.map((v) => Object.assign({}, v))
    }
    this.interval = Object.assign({}, this.interval)
    this.resetIdArray();
  }


  // reorder array when we drop within the same interval, transfer item when we drag items into other intervals.
  drop(event: CdkDragDrop<Interval[], Interval[]>): void {
    if (event.container === event.previousContainer) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex,
      );
    }
  }

  // converts allIDs to strings and assigns the result to allDragNDropIDs.
  // allDragNDropIDs is used by the cdkDropList elements to connect the different lists.
  // Note: since the order of this list is important, reverse() is necessary.
  resetIdArray() {
    this.allDragNDropIDs = this.allIDs
      .map((num) => `${num}`)
      .reverse()
  }

  // return if the child element of this interval is an array of intervals or a single Step object.
  isStep(): boolean {
    return !Array.isArray(this.interval.steps)
  }

  getIntervalArray(): Interval[] {
    if (Array.isArray(this.interval.steps)) {
      return this.interval.steps;
    }
    return [];
  }

  getStep(): Step {
    if (!Array.isArray(this.interval.steps)) {
      return this.interval.steps;
    }
    return null;
  }
}
