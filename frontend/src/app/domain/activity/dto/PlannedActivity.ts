import {
  convertToInterval,
  convertToIntervalSplit,
  Interval,
  IntervalSplit,
} from '../../../common/interval/dto/Interval';
import { Athlete } from "../../user/dto/Athlete";
import { User } from "../../user/dto/User";

export interface PlannedActivity {
  id: number;
  type: ActivityType;
  interval: Interval;
  withTrainer: boolean;
  template: boolean;
  note?: string;
  date?: Date;
  createdBy: User;
  createdFor?: Athlete;
}

export interface PlannedActivitySplit {
  id: number;
  type: ActivityType;
  interval: IntervalSplit;
  withTrainer: boolean;
  template: boolean;
  note?: string;
  date?: Date;
  createdBy: User;
  createdFor?: Athlete;
}

export enum ActivityType {
  SWIM = 'SWIM',
  RUN = 'RUN',
  BIKE = 'BIKE',
  ROW = 'ROW',
  CROSSCOUNTRYSKIING = 'CROSSCOUNTRYSKIING'
}

export const ActivityNameMapper = new Map<ActivityType, string>([
  [ActivityType.SWIM, 'Swim'],
  [ActivityType.RUN, 'Run'],
  [ActivityType.BIKE, 'Bike'],
  [ActivityType.ROW, 'Row'],
  [ActivityType.CROSSCOUNTRYSKIING, 'Cross-country Skiing'],
]);

export function convertToPlannedActivitySplit(plannedActivity: PlannedActivity): PlannedActivitySplit {
  return {
    id: plannedActivity.id,
    type: plannedActivity.type,
    interval: convertToIntervalSplit(plannedActivity.interval),
    withTrainer: plannedActivity.withTrainer,
    template: plannedActivity.template,
    note: plannedActivity.note,
    date: plannedActivity.date,
    createdBy: plannedActivity.createdBy,
    createdFor: plannedActivity.createdFor,
  };
}

export function convertToPlannedActivity(plannedActivity: PlannedActivitySplit): PlannedActivity {
  return {
    id: plannedActivity.id,
    type: plannedActivity.type,
    interval: convertToInterval(plannedActivity.interval),
    withTrainer: plannedActivity.withTrainer,
    template: plannedActivity.template,
    note: plannedActivity.note,
    date: plannedActivity.date,
    createdBy: plannedActivity.createdBy,
    createdFor: plannedActivity.createdFor,
  };
}
