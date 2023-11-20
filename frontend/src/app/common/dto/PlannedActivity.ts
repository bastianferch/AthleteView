import { convertToIntervalSplit, Interval, IntervalSplit } from '../interval/dto/Interval';

export interface PlannedActivity {
  id: number;
  type: ActivityType;
  interval: Interval;
  withTrainer: boolean;
  template: boolean;
  note?: string;
  date?: Date;
}

export interface PlannedActivitySplit {
  id: number;
  type: ActivityType;
  interval: IntervalSplit;
  withTrainer: boolean;
  template: boolean;
  note?: string;
  date?: Date;
}

export enum ActivityType {
  SWIM = 'SWIM',
  RUN = 'RUN',
  BIKE = 'BIKE',
  ROW = 'ROW',
  CROSSCOUNTRYSKIING = 'Cross-Country Skiing'
}

export function convertToPlannedActivitySplit(plannedActivity: PlannedActivity): PlannedActivitySplit {
  return {
    id: plannedActivity.id,
    type: plannedActivity.type,
    interval: convertToIntervalSplit(plannedActivity.interval),
    withTrainer: plannedActivity.withTrainer,
    template: plannedActivity.template,
    note: plannedActivity.note,
    date: plannedActivity.date,
  };
}
