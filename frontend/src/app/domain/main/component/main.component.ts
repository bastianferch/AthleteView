import { Component } from '@angular/core';
import { AuthService } from "../../auth/service/auth.service";
import { Router } from "@angular/router";
import { MatDialog } from "@angular/material/dialog";
import { FitImportDialogComponent } from 'src/app/fit-import-dialog/fit-import-dialog.component';
import { ActivityService } from '../../activities/service/activities.service';

@Component({
  selector: 'app-main',
  templateUrl: './main.component.html',
  styleUrls: ['./main.component.scss']
})
export class MainComponent {
  constructor(private authService: AuthService,
    private router: Router,
    private dialog: MatDialog,
    private activityService: ActivityService) {
  }
  logout(): void {
    this.authService.logout()
    this.router.navigate(['/auth/login'])
  }

  openImportDialog(): void {
    let dialogRef = this.dialog.open(FitImportDialogComponent, {
      width: "30%",
      data: {}
    });

    dialogRef.afterClosed().subscribe((files) => {
      if (files.length > 0) {
        // TODO: notification service
        this.activityService.importFitActivity(files).subscribe({
          next: (data) => {
            console.log("HTTP Call success! Data returned:")
            console.log(data)
          },
          error: (err) => console.error
        })
      }
    });
  }
}
