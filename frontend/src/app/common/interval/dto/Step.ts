
export interface Step {
  id:number;
  type: StepType;
  durationType: StepDurationType;
  duration?: number;
  durationUnit?: StepDurationUnit;
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
  TIME = "TIME",
}

export const StepDurationMapper = new Map<StepDurationType, string>([
  [StepDurationType.DISTANCE, 'Distance'],
  [StepDurationType.LAPBUTTON, 'Press Lap Button'],
  [StepDurationType.TIME, 'Time'],
]);


export enum StepDurationUnit {
  KM = "KM",
  M = "M",
  MIN = "MIN",
  SEC = "SEC"
}

export const StepDurationUnitMapper = new Map<StepDurationUnit, string>([
  [StepDurationUnit.KM, 'km'],
  [StepDurationUnit.M, 'm'],
  [StepDurationUnit.MIN, 'min'],
  [StepDurationUnit.SEC, 'sec'],
]);

export enum StepTargetType {
  CADENCE = "CADENCE",
  HEARTRATE = "HEARTRATE",
  PACE = "PACE",
  SPEED = "SPEED",
}

export const StepTargetMapper = new Map<StepTargetType, string>([
  [StepTargetType.CADENCE, 'Cadence'],
  [StepTargetType.HEARTRATE, 'Heart Rate'],
  [StepTargetType.PACE, 'Pace'],
  [StepTargetType.SPEED, 'Speed'],
]);
