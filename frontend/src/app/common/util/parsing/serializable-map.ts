import { map } from "rxjs/operators";

export class SerializableMap {
  static instanceType: any;

  static serializeResponseMap(): any {
    const createInstance = (r: any) => {
      return Object.assign(new this.instanceType(), r);
    };

    return map((respValue: any) => {
      if (Array.isArray(respValue)) {
        return respValue.map((r) => createInstance(r));
      }
      return createInstance(respValue);
    });
  }
}
