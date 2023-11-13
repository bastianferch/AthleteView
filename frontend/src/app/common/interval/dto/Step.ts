// TODO this should be somewhere else
export interface Step {
  type: string; // TODO probably an enum?
  duration_type: string; // TODO probably an enum?
  duration_distance?: number;
  duration_distance_unit?: string; // TODO probably an enum?
  target_type?: string; // TODO probably an enum?
  target_from?: number;
  target_to?: number;
  note?: string;
}
