import { Component, OnInit, Output, EventEmitter, Input } from '@angular/core';
import { TimeConstraint } from '../../../../common/dto/TimeConstraint';
import { TimeConstraintService } from '../../service/time-constraints.service';
import { SnackbarService } from '../../../../common/service/snackbar.service';


@Component({
  selector: 'app-create-time-constraints',
  templateUrl: './create-time-constraints.component.html',
  styleUrls: ['./create-time-constraints.component.scss'],
})
export class CreateTimeConstraintsComponent implements OnInit {

  @Input() constraint: TimeConstraint = { title: "", isBlacklist: true }
  startTime: string
  endTime: string
  date: Date
  weekly: boolean
  weekdays: any

  @Output() newConstraint = new EventEmitter<any>();

  constructor(private constraintService: TimeConstraintService, private msgService: SnackbarService) {}

  ngOnInit(): void {
    this.setUpInputs()
  }

  setUpInputs(): void {

    this.date = new Date()
    this.startTime = "12:00"
    this.endTime = "12:00"

    this.weekly = false
    this.weekdays = []

    this.constraint = { title: "", isBlacklist: true }
  }

  setBlacklist(val: boolean) {
    this.constraint.isBlacklist = val
  }

  setWeekly(val: boolean) {
    this.weekly = val
  }

  setWeekdays(val: any) {
    this.weekdays = val
  }

  setStartTime(val: string) {
    this.startTime = val
  }

  setEndTime(val:string) {
    this.endTime = val
  }

  setDate(val: any) {
    this.date = new Date(val.value)
  }

  saveConstraint() {
    if (this.startTime < this.endTime) {
      if (this.weekly) {
        for (const day of this.weekdays) {
          this.constraint.constraint = { weekday: day, startTime: this.startTime, endTime: this.endTime }
          this.constraintService.createWeeklyConstraint(this.constraint).subscribe(() => {
            this.newConstraint.emit()
            this.setUpInputs()
          },
          (error) => this.msgService.openSnackBar(error.error?.msg))
        }
      } else {
        this.constraint.startTime = this.makeDate(new Date(this.date), this.startTime)
        this.constraint.endTime = this.makeDate(new Date(this.date), this.endTime)
        this.constraintService.createDailyConstraint(this.constraint).subscribe(() => {
          this.newConstraint.emit()
          this.setUpInputs()
        },
        (error) => this.msgService.openSnackBar(error.error?.msg))
      }

    } else {
      this.msgService.openSnackBar('Invalid time constraints. Start date must be before end date.');
    }
  }

  makeDate(day: Date, time: string): Date {
    day.setHours(Number(time.split(":")[0]) - (new Date().getTimezoneOffset() / 60))
    day.setMinutes(Number(time.split(":")[1]))
    day.setSeconds(0)
    return day
  }
}
