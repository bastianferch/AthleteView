import { Injectable } from '@angular/core';
import { StepDurationType, StepTargetType, StepType } from "../dto/Step";

@Injectable({
  providedIn: 'root',
})
export class IntervalService {
  getColor(type?: StepType) {
    if (type === StepType.ACTIVE) return 'Crimson';
    if (type === StepType.RECOVERY) return 'LightGreen';
    if (type === StepType.WARMUP) return 'orange';
    if (type === StepType.COOLDOWN) return 'Turquoise';
    return "gray";
  }

  getUnitForTargetType(type?: StepTargetType) {
    if (type === StepTargetType.CADENCE) return "spm";
    if (type === StepTargetType.HEARTRATE) return "bpm";
    if (type === StepTargetType.PACE) return "min/km";
    if (type === StepTargetType.SPEED) return "km/h";
    return "";
  }

  convertToSeconds(minSec: string): number {
    const minSecArray = minSec.split(':');
    return parseInt(minSecArray[0], 10) * 60 + parseInt(minSecArray[1], 10);
  }

  convertToMinSec(seconds: number): string {
    const min = Math.floor(seconds / 60);
    const sec = seconds % 60;
    return `${min}:${sec < 10 ? '0' : ''}${sec}`;
  }

  showIntensitySettings(targetType: StepTargetType): boolean {
    return !(targetType === undefined);
  }

  showDurationSettings(durationType: StepDurationType): boolean {
    return !(durationType === StepDurationType.LAPBUTTON) && !(durationType === undefined);
  }
}
