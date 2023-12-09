import { Component } from '@angular/core';
import { NgFor } from "@angular/common";
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { FormsModule } from "@angular/forms";
import { MatFormFieldModule } from "@angular/material/form-field";
import { Zone } from '../dto/Zone'
import { MatInputModule } from "@angular/material/input";
import { MatButtonModule } from "@angular/material/button";

@Component({
  selector: 'app-zone-groups-dialog',
  templateUrl: './zone-groups-dialog.component.html',
  imports: [MatDialogModule, MatFormFieldModule, MatInputModule, FormsModule, MatButtonModule, NgFor],
  styleUrls: ['./zone-groups-dialog.component.scss'],
  standalone: true,
})
export class ZoneGroupsDialogComponent {

  zones: Zone[]
  constructor(public dialogRef: MatDialogRef<ZoneGroupsDialogComponent>) {
    // TODO: remove (this is temporary)
    this.zones = [{ name: 'Low',fromBPM: 0, toBPM: 80 },
      { name: 'Medium',fromBPM: 81, toBPM: 125 },
      { name: 'High',fromBPM: 126, toBPM: 300 }]
  }


  reset(): void {
    // TODO implement
    // console.log('\'ello!')
  }

  addZone(): void {
    this.zones.push({ name: 'New Zone',fromBPM: 0,toBPM: 100 })
  }

  removeZone(zone: Zone): void {
    this.zones = this.zones.filter((value) => value !== zone)
  }

  handleCancel(): void {
    this.dialogRef.close();
  }
}
