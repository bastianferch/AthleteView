
export interface Step {
  id:number;
  type: StepType;
  durationType: StepDurationType;
  durationDistance?: number;
  durationDistanceUnit?: StepDurationDistanceUnit;
  targetType?: StepTargetType;
  targetFrom?: number;
  targetTo?: number;
  note?: string;
}


export enum StepType {
  ACTIVE = "ACTIVE",
  RECOVERY = "RECOVERY",
  WARMUP = "WARMUP",
  COOLDOWN = "COOLDOWN",
}

export const StepNameMapper = new Map<StepType, string>([
  [StepType.ACTIVE, 'Active'],
  [StepType.RECOVERY, 'Recovery'],
  [StepType.WARMUP, 'Warm up'],
  [StepType.COOLDOWN, 'Cool Down'],
]);

export enum StepDurationType {
  DISTANCE = "DISTANCE",
  LAPBUTTON = "LAPBUTTON",
}

export const StepDurationMapper = new Map<StepDurationType, string>([
  [StepDurationType.DISTANCE, 'Distance'],
  [StepDurationType.LAPBUTTON, 'Press Lap Button'],
]);


export enum StepDurationDistanceUnit {
  KM = "KM",
  M = "M",
}

export const StepDurationDistanceUnitMapper = new Map<StepDurationDistanceUnit, string>([
  [StepDurationDistanceUnit.KM, 'km'],
  [StepDurationDistanceUnit.M, 'm'],
]);

export enum StepTargetType {
  CADENCE = "CADENCE",
  HEARTRATE = "HEARTRATE",
}

export const StepTargetMapper = new Map<StepTargetType, string>([
  [StepTargetType.CADENCE, 'Cadence'],
  [StepTargetType.HEARTRATE, 'Heart Rate'],
]);
