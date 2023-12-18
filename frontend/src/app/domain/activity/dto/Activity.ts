import { Lap } from "src/app/common/interval/dto/Lap";
import { ActivityType, PlannedActivity } from "./PlannedActivity";

export interface Activity {
  id: number;
  accuracy: number;
  averageBpm: number;
  minBpm: number;
  maxBpm: number;
  distance: number;
  spentKcal: number;
  cadence: number;
  avgPower: number;
  maxPower: number;
  load: number;
  fatigue: number;
  fitData: string;
  startTime?: Date | number[];
  endTime?: Date | number[];
  activityType?: ActivityType;
  laps?: Lap[];
  plannedActivity?: PlannedActivity;
}

export interface ActivityEvent {
  id: number;
  accuracy: number;
  averageBpm: number;
  minBpm: number;
  maxBpm: number;
  distance: number;
  spentKcal: number;
  cadence: number;
  avgPower: number;
  maxPower: number;
  load: number;
  fatigue: number;
  fitData: string;
  startTime?: Date | number[];
  endTime?: Date | number[];
  activityType?: ActivityType;
  laps?: Lap[];
  plannedActivity?: PlannedActivity;
  objectType: string;
}
