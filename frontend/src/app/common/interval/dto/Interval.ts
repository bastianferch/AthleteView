import { Step } from "./Step";

export interface Interval {
  id?: number,
  dbId?:number
  repeat: number,
  steps: Interval[] | Step,
}
export interface IntervalSplit {
  id?: number,
  repeat: number,
  intervals: IntervalSplit[],
  step: Step,
}


export function convertToIntervalSplit(interval: Interval): IntervalSplit {

  const intervalSplit: IntervalSplit = {
    id: interval.dbId,
    repeat: interval.repeat,
    intervals: [],
    step: {} as Step, // Define your Step object accordingly
  };

  if (Array.isArray(interval.steps)) {
    intervalSplit.intervals = interval.steps.map((stepOrInterval) =>
      (isInterval(stepOrInterval) ? convertToIntervalSplit(stepOrInterval as Interval) : {
        repeat: (stepOrInterval as Interval).repeat,
        intervals: [],
        step: stepOrInterval as Step,
      }),
    );
  } else {
    intervalSplit.step = interval.steps as Step;
  }

  return intervalSplit;
}

function isInterval(obj: Interval | Step): obj is Interval {
  return (obj as Interval).steps !== undefined;
}

export function convertToInterval(intervalSplit: IntervalSplit): Interval {
  const interval: Interval = {
    id: undefined,
    dbId: intervalSplit.id,
    repeat: intervalSplit.repeat,
    steps: intervalSplit.intervals.length > 0 ?
      intervalSplit.intervals.map(convertToInterval) : intervalSplit.step,
  };

  return interval;
}
