import { Interval } from '../interval/dto/Interval';

export interface PlannedActivity {
  type: ActivityType;
  interval: Interval;
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
