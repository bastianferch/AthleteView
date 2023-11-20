import { Component, OnInit } from '@angular/core';
import { WeeklyTimeConstraint, DailyTimeConstraint } from '../../../../common/dto/TimeConstraint';
import { TimeConstraintService } from '../../service/time-constraints.service';
import { Output, EventEmitter } from '@angular/core';


@Component({
  selector: 'app-create-time-constraints',
  templateUrl: './create-time-constraints.component.html',
  styleUrls: ['./create-time-constraints.component.scss']
})
export class CreateTimeConstraintsComponent implements OnInit {

  constructor(private constraintService: TimeConstraintService) {
  }

  title: string = ""
  startTime: string
  endTime: string
  date: Date
  isBlacklist: boolean
  weekly: boolean
  weekdays: any

  @Output() newConstraint = new EventEmitter<any>();

  ngOnInit(): void {
    this.setUpInputs()
  }

  setUpInputs(): void {

    this.date = new Date()
    this.title = ""
    this.startTime = "12:00"
    this.endTime = "12:00"

    this.isBlacklist = true
    this.weekly = false
    this.weekdays=[]
  }

  setBlacklist(val: boolean) {
    this.isBlacklist = val
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
      if(this.weekly) {
        for (let day of this.weekdays) {
          let weeklyConstraint: WeeklyTimeConstraint = {isBlacklist: this.isBlacklist, title: this.title,
                                                        constraint: {weekday:day, startTime: this.startTime, endTime: this.endTime}}
          this.constraintService.createWeeklyConstraint(weeklyConstraint).subscribe(next => {
            console.log(next)
            this.newConstraint.emit()
            this.setUpInputs()
          })
        }
      }
      else {
        let dailyConstraint: DailyTimeConstraint = {isBlacklist: this.isBlacklist, title: this.title,
                                                    startTime: this.makeDate(new Date(this.date), this.startTime), endTime: this.makeDate(new Date(this.date), this.endTime)}
        this.constraintService.createDailyConstraint(dailyConstraint).subscribe(next => {
          console.log(next)
          this.newConstraint.emit()
          this.setUpInputs()
        })
      }

    } else {
      console.error('Invalid time constraints. Start date must be before end date.');
    }
  }

  makeDate(day: Date, time: string): Date {
    day.setHours(parseInt(time.split(":")[0]))
    day.setMinutes(parseInt(time.split(":")[1]))
    day.setSeconds(0)
    return day
  }
}
