import { Component } from '@angular/core';
import { AuthService } from "../../auth/service/auth.service";
import { Router } from "@angular/router";
import { NotificationService } from "../../notification/service/notification.service";
import { MatDrawer } from "@angular/material/sidenav";
import { MatDialog } from "@angular/material/dialog";
import { FitImportDialogComponent } from 'src/app/fit-import-dialog/fit-import-dialog.component';
import { ActivityService } from '../../activity/service/activity.service';
import { SnackbarService } from "../../../common/service/snackbar.service";
import { HealthService } from "../../health/service/health.service";
import { firstValueFrom } from "rxjs";
import { ZoneGroupsDialogComponent } from "../../zone-groups-dialog/component/zone-groups-dialog.component";
import { InviteDialogComponent } from '../../invite-dialog/invite-dialog.component';
import { PreferencesDialogComponent } from "../../preferences-dialog/preferences-dialog.component";
import { UserService } from "../../user/service/UserService";

@Component({
  selector: 'app-main',
  templateUrl: './main.component.html',
  styleUrls: ['./main.component.scss'],
})
export class MainComponent {
  email: string = this.authService.currentUser.email
  currentUserType = this.authService.currentUser.userType
  constructor(
    private authService: AuthService,
    private notificationService: NotificationService,
    private router: Router,
    private dialog: MatDialog,
    private userService:UserService ,
    private activityService: ActivityService,
    private snackbarService: SnackbarService,
    private healthService: HealthService) {
  }

  markAllNotificationsAsRead() {
    this.notificationService.markAllNotificationsAsRead();
  }

  onOpen(drawer: MatDrawer) {
    // give the notification service a reference to the drawer when first opening it
    /*    if (this.notificationService.notificationDrawer !== null) return;*/
    this.notificationService.notificationDrawer = drawer
  }

  getNumberOfUnreadNotifications() {
    return this.notificationService.notifications.filter((n) => n.read === false).length;
  }

  logout(): void {
    this.authService.logout()
    this.router.navigate(['/auth/login'])
  }

  async mockHealthData(): Promise<void> {
    await firstValueFrom(this.healthService.mock());
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

  openInviteDialog(): void {
    this.dialog.open(InviteDialogComponent, {
      width: "60%",
    })
  }

  openZoneDialog(): void {
    this.dialog.open(ZoneGroupsDialogComponent, {
      width: "60%",
    });
  }

  openPreferencesDialog(): void {
    this.dialog.open(PreferencesDialogComponent, {
      width: "30%",
    });
  }
}
