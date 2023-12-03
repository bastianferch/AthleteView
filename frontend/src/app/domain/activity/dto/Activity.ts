export interface Activity {
  id: number;
  accuracy: number;
  averageBpm: number;
  maxBpm: number;
  distance: number;
  spentKcal: number;
  cadence: number;
  avgPower: number;
  maxPower: number;
  load: number;
  fatigue: number;
  fitData: string;
  startTime: number[];
  endTime: number[];
}
