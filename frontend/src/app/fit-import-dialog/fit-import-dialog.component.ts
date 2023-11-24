import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
  selector: 'app-fit-import-dialog',
  templateUrl: './fit-import-dialog.component.html',
  styleUrls: ['./fit-import-dialog.component.scss']
})
export class FitImportDialogComponent {
  constructor(public dialogRef: MatDialogRef<FitImportDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any) { }

  files: String[] = []

  // workaround for <input type='file'>
  onFileSelected(event: any): void {
    if (event.target.files[0] != null) {
      this.files.push(event.target.files[0])
    }
    console.log(`found total of ${this.files.length} files!`)
  }

  handleCancel(): void {
    this.dialogRef.close();
  }

  handleUpload(data: any) {
    console.log('totally doing stuff')
  }
}
