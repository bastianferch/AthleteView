import { Component, OnInit } from '@angular/core';
import { NgFor } from "@angular/common";
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { FormsModule } from "@angular/forms";
import { MatFormFieldModule } from "@angular/material/form-field";
import { Zone } from '../dto/Zone'
import { MatInputModule } from "@angular/material/input";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { ZoneService } from "../service/zone.service";
import { SnackbarService } from "../../../common/service/snackbar.service";

@Component({
  selector: 'app-zone-groups-dialog',
  templateUrl: './zone-groups-dialog.component.html',
  imports: [MatDialogModule, MatFormFieldModule, MatInputModule, FormsModule, MatButtonModule, NgFor, MatIconModule],
  styleUrls: ['./zone-groups-dialog.component.scss'],
  standalone: true,
})
export class ZoneGroupsDialogComponent implements OnInit {

  zones: Zone[]
  constructor(public dialogRef: MatDialogRef<ZoneGroupsDialogComponent>,
    public zoneService: ZoneService,
    public msgService: SnackbarService) {}


  ngOnInit(): void {
    this.zoneService.getZones().subscribe({
      next: (next: Zone[]) => this.zones = next,
      error: (e) => this.msgService.openSnackBar(e.error?.message),
    },
    )
  }

  reset(): void {
    this.zoneService.resetZones().subscribe({
      next: (next: Zone[]) => this.zones = next,
      error: (e) => this.msgService.openSnackBar(e.error?.message),
    },
    )
  }

  addZone(): void {
    const top: number = this.zones[this.zones.length - 1].toBPM
    this.zones.push({ name: 'New Zone',fromBPM: top,toBPM: top + 20 })
  }

  removeZone(zone: Zone): void {
    const middle: number = Math.round((zone.toBPM + zone.fromBPM) / 2)
    const index: number = this.zones.indexOf(zone)
    switch (index) {
      case 0:
        this.zones[1].fromBPM = 0
        break
      case (this.zones.length - 1):
        break
      default:
        this.zones[index - 1].toBPM = middle
        this.zones[index + 1].fromBPM = middle
    }
    this.zones = this.zones.filter((value) => value !== zone)
  }

  save(): void {
    this.zoneService.editZones(this.zones).subscribe({
      next: (next: Zone[]) => {
        this.zones = next
        this.handleCancel()
      },
      error: (e) => {
        this.msgService.openSnackBar(e.error?.message)
      },
    },
    )
  }

  handleCancel(): void {
    this.dialogRef.close();
  }
}
