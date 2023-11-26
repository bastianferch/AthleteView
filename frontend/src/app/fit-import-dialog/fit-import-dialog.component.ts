import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
  selector: 'app-fit-import-dialog',
  templateUrl: './fit-import-dialog.component.html',
  styleUrls: ['./fit-import-dialog.component.scss'],
})
export class FitImportDialogComponent {
  files: File[] = []


  constructor(public dialogRef: MatDialogRef<FitImportDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any) { }

  // workaround for <input type='file'>
  onFileSelected(event: any): void {
    if (event.target.files[0] !== null) {
      for (const item of event.target.files) {
        const exists = this.files.filter((x) => x.name === item.name)
        if (exists.length === 0) {
          this.files.push(item)
        }
      }
    }
  }

  handleCancel(): void {
    this.dialogRef.close();
  }
}
