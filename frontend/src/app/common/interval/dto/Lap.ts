import { StepTargetType } from "./Step";

export interface Lap {
  id?: number;
  lapNum: number;
  time?: number; // in ds (10^-1 s)
  distance?: number; // in m
  avgSpeed?: number; // in m/s
  avgPower?: number; // in W
  maxPower?: number; // in W
  avgBpm: number;
  maxBpm: number;
  avgCadence?: number;
  maxCadence?: number;
  stepType?: StepTargetType;
}
