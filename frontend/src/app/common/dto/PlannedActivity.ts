import { convertToIntervalSplit, Interval, IntervalSplit } from '../interval/dto/Interval';

export interface PlannedActivity {
  id: number;
  type: ActivityType;
  interval: Interval;
  with_trainer: boolean;
  template: boolean;
  note?: string;
  date?: Date;
}

export interface PlannedActivitySplit {
  id: number;
  type: ActivityType;
  interval: IntervalSplit;
  with_trainer: boolean;
  template: boolean;
  note?: string;
  date?: Date;
}

export enum ActivityType {
  SWIM = 'Swim',
  RUN = 'Run',
  BIKE = 'Bike',
  STRENGTH = 'Strength',
  ROW = 'Row',
  CROSSCOUNTRY = 'Cross-Country'
}

export function convertToPlannedActivitySplit(plannedActivity: PlannedActivity): PlannedActivitySplit {
  return {
    id: plannedActivity.id,
    type: plannedActivity.type,
    interval: convertToIntervalSplit(plannedActivity.interval),
    with_trainer: plannedActivity.with_trainer,
    template: plannedActivity.template,
    note: plannedActivity.note,
    date: plannedActivity.date,
  };
}
