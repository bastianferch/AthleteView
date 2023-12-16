import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable({
  providedIn: 'root',
})
export class SnackbarService {

  constructor(private _snackBar: MatSnackBar) {
  }

  openSnackBar(msg: string) {
    this._snackBar.open(msg, '', {
      horizontalPosition: 'right',
      verticalPosition: 'bottom',
      duration: 3000,
    });
  }

  openSnackBarWithAction(msg: string, action: string) {
    const snackBarRef = this._snackBar.open(msg, action, {
      horizontalPosition: 'right',
      verticalPosition: 'bottom',
      duration: 3000,
    });
    return snackBarRef.onAction();
  }
}
