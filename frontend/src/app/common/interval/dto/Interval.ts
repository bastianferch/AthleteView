import { Step } from "./Step";

export interface Interval {
  id?: number,
  repeat: number,
  steps: Interval[] | Step,
}
