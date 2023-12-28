import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-dialog',
  templateUrl: './confirmation-dialog.component.html',
  styleUrls: ['./confirmation-dialog.component.scss'],
})
export class ConfirmationDialogComponent {

  headline: string;
  content: string;
  action: string;


  constructor(@Inject(MAT_DIALOG_DATA) public injectedData:
  any, private dialogRef: MatDialogRef<ConfirmationDialogComponent>) {
    this.headline = this.injectedData.headline;
    this.content = this.injectedData.content;
    this.action = this.injectedData.action;
  }

  cancelDialog() {
    this.dialogRef.close(false);
  }

  confirmDialog() {
    this.dialogRef.close(true);
  }

}
