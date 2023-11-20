export interface WeeklyTimeConstraint {
    id?: number,
    isBlacklist: boolean,
    title: string,
    user?: any,
    constraint: TimeFrame
}

export interface DailyTimeConstraint {
  id?: number,
  isBlacklist: boolean,
  title: string,
  user?: any,
  startTime: Date,
  endTime: Date
}

export interface TimeFrame {
  weekday: WeekDay,
  startTime: string,
  endTime: string
}

export enum WeekDay {
  MONDAY,
  TUESDAY,
  WEDNESDAY,
  THURSDAY,
  FRIDAY,
  SATURDAY,
  SUNDAY
}
