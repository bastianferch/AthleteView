import { SerializableMap } from "../../../common/util/parsing/serializable-map";

export class User extends SerializableMap {
  static override instanceType = User;
  name: string;
  email: string;
  token: string;
  country: string;
  zip: string;
  userType: 'athlete' | 'trainer';
  id?: number;
  password?: string;

  isAthlete(): boolean {
    return this.userType === 'athlete'
  }
}

export class Trainer extends User {
  override userType: 'trainer';
  code: string;
  athletes: Athlete[];
}

export class Athlete extends User {
  override userType: 'athlete';
  dob: Date;
  height: number;
  weight: number;
  trainer: Trainer
}
