export interface TimeConstraint {
  id?: number,
  isBlacklist: boolean,
  title: string,
  user?: any,
  constraint?: TimeFrame,
  startTime?: Date | number[],
  endTime?: Date | number[]
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
