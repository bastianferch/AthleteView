import { Injectable } from '@angular/core';
import { StepTargetType, StepType } from "../dto/Step";

@Injectable({
  providedIn: 'root',
})
export class IntervalService {

  // TODO these colors are not dependent on the theme colors. But they could be dependent on light/dark mode.
  getColor(type?: StepType) {
    if (type === StepType.RUN) return 'Crimson';
    if (type === StepType.RECOVERY) return 'LightGreen';
    if (type === StepType.WARMUP) return 'orange';
    if (type === StepType.COOLDOWN) return 'Turquoise';
    return "gray";
  }

  getUnitForTargetType(type?: StepTargetType) {
    if (type === StepTargetType.CADENCE) return "spm";
    if (type === StepTargetType.HEARTRATE) return "bpm";
    return "";
  }
}
