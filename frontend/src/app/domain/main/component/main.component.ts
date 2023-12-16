import { Component } from '@angular/core';
import { AuthService } from "../../auth/service/auth.service";
import { Router } from "@angular/router";
import { MatDialog } from "@angular/material/dialog";
import { FitImportDialogComponent } from 'src/app/fit-import-dialog/fit-import-dialog.component';
import { ActivityService } from '../../activity/service/activity.service';
import { SnackbarService } from "../../../common/service/snackbar.service";
import { ZoneGroupsDialogComponent } from "../../zone-groups-dialog/component/zone-groups-dialog.component";

@Component({
  selector: 'app-main',
  templateUrl: './main.component.html',
  styleUrls: ['./main.component.scss'],
})
export class MainComponent {
  constructor(private authService: AuthService,
    private router: Router,
    private dialog: MatDialog,
    private activityService: ActivityService, private snackbarService: SnackbarService) {
  }

  logout(): void {
    this.authService.logout()
    this.router.navigate(['/auth/login'])
  }

  openImportDialog(): void {
    const dialogRef = this.dialog.open(FitImportDialogComponent, {
      width: "30%",
      data: {},
    });

    dialogRef.afterClosed().subscribe((files) => {
      if (files.length > 0) {
        this.activityService.importFitActivity(files).subscribe({
          next: () => {
            this.snackbarService.openSnackBar("Imported data successfully!")
          },
          error: () => {
            this.snackbarService.openSnackBarWithAction("Error importing data!", "Close")
          },
        })
      }
    });
  }

  openZoneDialog(): void {
    this.dialog.open(ZoneGroupsDialogComponent, {
      width: "60%",
    });
  }
}
