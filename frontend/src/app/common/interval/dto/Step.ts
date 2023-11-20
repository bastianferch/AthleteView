// TODO this should be somewhere else
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

export enum StepDurationType {
  DISTANCE = "DISTANCE",
  LAPBUTTON = "LAPBUTTON",
}


export enum StepDurationDistanceUnit {
  KM = "KM",
}

export enum StepTargetType {
  CADENCE = "CADENCE",
  HEARTRATE = "HEARTRATE",
}
