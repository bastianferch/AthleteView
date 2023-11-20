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
  RUN = "run",
  RECOVERY = "recovery",
  WARMUP = "warm up",
  COOLDOWN = "cool down",
}

export enum StepDurationType {
  DISTANCE = "distance",
  LAPBUTTON = "lap button press",
}


export enum StepDurationDistanceUnit {
  KM = "km",
}

export enum StepTargetType {
  CADENCE = "cadence",
  HEARTRATE = "heartrate",
}
