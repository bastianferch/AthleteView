import { NativeDateAdapter } from '@angular/material/core';

export class AppDateAdapter extends NativeDateAdapter {
  override parse(value: any): Date | null {
    let dateParts = value.split("/");
    if (dateParts.length !== 3) {
      dateParts = value.split("-");
    }
    if (dateParts.length !== 3) {
      return null;
    }
    if (dateParts[2] >= 0 && dateParts[2] <= 50) {
      dateParts[2] = '20' + dateParts[2];
    }

    return new Date(dateParts[2], dateParts[1] - 1, Number(dateParts[0]));
  }
}
