import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
  selector: 'app-my-modal',
  template: `
    <h2 mat-dialog-title>{{ data.title }}</h2>
    <mat-dialog-content>
      <p>{{ data.content }}</p>
    </mat-dialog-content>
    <mat-dialog-actions>
      <button mat-button (click)="onClose(true)">{{data.option1}}</button>
      <button mat-button (click)="onClose(false)">{{data.option2}}</button>
    </mat-dialog-actions>
  `,
})
export class ModalComponent {
  constructor(
    public dialogRef: MatDialogRef<ModalComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { title: string, content: string, option1:string, option2:string },
  ) { }

  onClose(action: boolean): void {
    this.dialogRef.close(action);
  }
}
