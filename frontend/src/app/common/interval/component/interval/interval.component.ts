import {
  ChangeDetectionStrategy,
  Component, EventEmitter, Inject,
  Input,
  OnChanges,
  OnInit, Output,
} from '@angular/core';
import { Step } from "../../dto/Step";
import { Interval } from "../../dto/Interval";
import { CdkDragDrop, moveItemInArray, transferArrayItem } from "@angular/cdk/drag-drop";
import { IntervalService } from "../../service/interval.service";
import { MAT_DIALOG_DATA, MatDialog, MatDialogModule, MatDialogRef } from "@angular/material/dialog";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { FormsModule } from "@angular/forms";
import { MatButtonModule } from "@angular/material/button";
import { MatSelectModule } from "@angular/material/select";
import { NgForOf, NgIf, NgStyle } from "@angular/common";
import { MatDividerModule } from "@angular/material/divider";
import { ActivityType } from "../../../dto/PlannedActivity";

@Component({
  selector: 'app-interval',
  templateUrl: './interval.component.html',
  styleUrls: ['./interval.component.scss', '../../style/interval.scss'],
  changeDetection: ChangeDetectionStrategy.Default,
})
export class IntervalComponent implements OnInit, OnChanges {
  @Input() editable: boolean;
  @Input() topLevelInterval?: boolean;
  @Input() activityType: ActivityType;
  @Input() interval: Interval;
  @Input() rootInterval: Interval;
  @Input() allIDs: number[];
  @Input() level: number;
  @Input() maxNesting: number;
  @Output() deleteInterval: EventEmitter<number> = new EventEmitter();
  @Output() changeInterval: EventEmitter<Interval> = new EventEmitter();

  // keeps track of the IDs of the other (nested) drag&drop lists, so they can be connected.
  allDragNDropIDs: string[] = [];

  constructor(public dialog: MatDialog, protected service: IntervalService) {}

  ngOnInit(): void {
    this.resetIdArray();
  }

  // when the inputs change, update the ID array and update the interval so the sub-intervals are also notified about the change.
  ngOnChanges() {
    if (Array.isArray(this.interval.steps)) {
      // this is necessary to trigger change detection in the sub-intervals
      this.interval.steps = this.interval.steps.map((v) => Object.assign({}, v))
    }
    this.interval = Object.assign({}, this.interval);
    this.resetIdArray();
  }

  // open the edit dialog to change the number of repeats
  openDialog(): void {
    const dialogRef = this.dialog.open(EditStepDialogComponent, {
      data: this.interval,
    });

    dialogRef.afterClosed().subscribe((result: Interval) => {
      if (result !== undefined) {
        this.changeInterval.emit(result);
      }
    });
  }

  // reorder array when we drop within the same interval, transfer item when we drag items into other intervals.
  drop(event: CdkDragDrop<Interval[], Interval[]>): void {
    // cant drag an interval into the 2nd level
    if (this.level >= this.maxNesting && Array.isArray(event.item.data.steps)) {
      return;
    }

    // cant drag anything into the 3rd level
    if (this.level > this.maxNesting) {
      return;
    }

    // cant pre-nest items and then drag them into another interval if the overall level would be > 2
    const containerMaxLevel = this.getMaxLevel(event.item.data.id)
    if (this.level + containerMaxLevel - 1 > this.maxNesting) {
      // TODO notify user that max nesting limit is reached
      return;
    }

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

  // get the depth of an interval with id
  getMaxLevel(id: number) {

    const getIntervalWithId = (intId: number, int: Interval): Interval => {
      if (int.id === intId) return int;
      if (Array.isArray(int.steps)) {
        for (const step of int.steps) {
          const temp = getIntervalWithId(intId, step);
          if (temp !== null) return temp;
        }
      }
      return null;
    }

    const getNumberOfLevels = (currentInterval: Interval, currentLevel: number): number => {
      if (Array.isArray(currentInterval.steps)) {
        let max = 1;
        for (const step of currentInterval.steps) {
          const temp = getNumberOfLevels(step, currentLevel + 1);
          if (temp > max) max = temp;
        }
        return currentLevel + max
      }
      return currentLevel - 1;
    }
    return getNumberOfLevels(getIntervalWithId(id, this.rootInterval), 1);
  }

  onDeleteInterval() {
    this.deleteInterval.emit(this.interval.id);
  }

  onChangeStep(newStep: Step) {
    const newInterval: Interval = Object.assign({}, this.interval);
    newInterval.steps = newStep;
    this.changeInterval.emit(newInterval);
  }

  handleDeleteInterval(deleteId: number) {
    // propagate to the higher levels
    this.deleteInterval.emit(deleteId);
  }

  handleChangeInterval(changedInterval: Interval) {
    const newInterval: Interval = Object.assign({}, this.interval);
    if (Array.isArray(newInterval.steps)) {
      newInterval.steps = newInterval.steps.map((subInterval) =>
        (subInterval.id === changedInterval.id ? changedInterval : subInterval));
    }
    this.changeInterval.emit(newInterval);
  }

  // converts allIDs to strings and assigns the result to allDragNDropIDs.
  // allDragNDropIDs is used by the cdkDropList elements to connect the different lists.
  // Note: since the order of this list is important, reverse() is necessary.
  resetIdArray() {
    this.allDragNDropIDs = this.allIDs
      .map((num) => `${num}`)
      .reverse()
  }

  // only render the interval card if it has more than one child components
  displayIntervalCard(): boolean {
    return !this.topLevelInterval && Array.isArray(this.interval.steps);

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


// dialog to edit the number of repeats of this interval
@Component({
  selector: 'app-edit-interval-dialog',
  template: `
    <h1 mat-dialog-title>Edit Interval</h1>
    <div mat-dialog-content>
      <mat-form-field appearance="outline">
        <mat-label>Repeats</mat-label>
        <input matInput type="number" [(ngModel)]="intervalCopy.repeat">
      </mat-form-field>
    </div>
    <div mat-dialog-actions [ngStyle]="{'display': 'flex', 'justify-content': 'flex-end', 'gap': '15px'}">
      <button mat-button (click)="onNoClick()">Cancel</button>
      <button mat-button color="primary" [mat-dialog-close]="intervalCopy">Save</button>
    </div>
  `,
  imports: [MatDialogModule, MatFormFieldModule, MatInputModule, FormsModule, MatButtonModule, MatSelectModule, NgForOf, NgIf, MatDividerModule, NgStyle],
  standalone: true,
})
export class EditStepDialogComponent implements OnInit {

  intervalCopy: Interval;

  constructor(
    public dialogRef: MatDialogRef<EditStepDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Interval,
  ) {}

  // copy the object aso the original object is not mutated
  ngOnInit(): void {
    this.intervalCopy = Object.assign({}, this.data);
  }

  // closes the dialog with result == undefined, so no changes are performed
  onNoClick(): void {
    this.dialogRef.close(this.data);
  }
}
