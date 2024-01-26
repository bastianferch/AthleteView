export interface ActivityStatsDto {
  bpm: number;
  speed: number;
  power: number;
  cadence: number;
  altitude: number;
  timestamp: Array<number> | Date;
}
