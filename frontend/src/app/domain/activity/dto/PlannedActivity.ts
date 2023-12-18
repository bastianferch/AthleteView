import {
  convertToInterval,
  convertToIntervalSplit,
  Interval,
  IntervalSplit,
} from '../../../common/interval/dto/Interval';
import { Athlete, User } from "../../user/dto/user";
import { Activity } from "./Activity";

export interface PlannedActivity {
  id: number;
  name: string;
  type: ActivityType;
  interval: Interval;
  withTrainer: boolean;
  template: boolean;
  note?: string;
  date?: Date | number[];
  estimatedDuration: number;
  load?: Load;
  createdBy: User;
  createdFor?: Athlete;
  activity?: Activity;
}

export interface PlannedActivitySplit {
  id: number;
  name: string;
  type: ActivityType;
  interval: IntervalSplit;
  withTrainer: boolean;
  template: boolean;
  note?: string;
  date?: Date | number[];
  estimatedDuration: number;
  load?: Load;
  createdBy?: User;
  createdFor?: Athlete;
  activity?: Activity;
}

export interface PlannedActivityEvent {
  id: number;
  name: string;
  type: ActivityType;
  interval: Interval;
  withTrainer: boolean;
  template: boolean;
  note?: string;
  date?: Date | number[];
  estimatedDuration: number;
  load?: Load;
  createdBy: User;
  createdFor?: Athlete;
  activity?: Activity;
  objectType: string;
}


export enum ActivityType {
  SWIM = 'SWIM',
  RUN = 'RUN',
  BIKE = 'BIKE',
  ROW = 'ROW',
  CROSSCOUNTRYSKIING = 'CROSSCOUNTRYSKIING'
}

export enum Load {
  EASY = 'EASY',
  MEDIUM = 'MEDIUM',
  HARD = 'HARD'
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
    name: plannedActivity.name,
    type: plannedActivity.type,
    interval: convertToIntervalSplit(plannedActivity.interval),
    withTrainer: plannedActivity.withTrainer,
    template: plannedActivity.template,
    note: plannedActivity.note,
    date: plannedActivity.date,
    estimatedDuration: plannedActivity.estimatedDuration,
    load: plannedActivity.load,
    createdBy: plannedActivity.createdBy,
    createdFor: plannedActivity.createdFor,
    activity: plannedActivity.activity,
  };
}

export function convertToPlannedActivity(plannedActivity: PlannedActivitySplit): PlannedActivity {
  return {
    id: plannedActivity.id,
    name: plannedActivity.name,
    type: plannedActivity.type,
    interval: convertToInterval(plannedActivity.interval),
    withTrainer: plannedActivity.withTrainer,
    template: plannedActivity.template,
    note: plannedActivity.note,
    date: plannedActivity.date,
    estimatedDuration: plannedActivity.estimatedDuration,
    load: plannedActivity.load,
    createdBy: plannedActivity.createdBy,
    createdFor: plannedActivity.createdFor,
    activity: plannedActivity.activity,
  };
}
