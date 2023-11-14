// TODO this should be somewhere else
export interface Step {
  type: StepType;
  duration_type: StepDurationType;
  duration_distance?: number;
  duration_distance_unit?: StepDurationDistanceUnit;
  target_type?: StepTargetType;
  target_from?: number;
  target_to?: number;
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
}
