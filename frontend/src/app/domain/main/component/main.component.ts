import { Component } from '@angular/core';
import { AuthService } from "../../auth/service/auth.service";
import { Router } from "@angular/router";
import { NotificationService } from "../../notification/service/notification.service";
import { MatDrawer } from "@angular/material/sidenav";
import { MatDialog } from "@angular/material/dialog";
import { FitImportDialogComponent } from 'src/app/fit-import-dialog/fit-import-dialog.component';
import { ActivityService } from '../../activity/service/activity.service';
import { SnackbarService } from "../../../common/service/snackbar.service";
import { ZoneGroupsDialogComponent } from "../../zone-groups-dialog/component/zone-groups-dialog.component";
import { InviteDialogComponent } from '../../invite-dialog/invite-dialog.component';
import { PreferencesDialogComponent } from "../../preferences-dialog/preferences-dialog.component";
import { MobileCheckService } from 'src/app/common/service/mobile-checker.service';

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
    private activityService: ActivityService,
    private snackbarService: SnackbarService,
    private mobileChecker: MobileCheckService,
  ) {
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

  openImportDialog(): void {
    const dialogRef = this.dialog.open(FitImportDialogComponent, {
      width: this.mobileChecker.isMobile() ? "90%" : "30%",
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
      width: this.mobileChecker.isMobile() ? "90%" : "60%",
    })
  }

  openZoneDialog(): void {
    if (this.mobileChecker.isMobile()) {
      this.dialog.open(ZoneGroupsDialogComponent, {
        width: "100%",
        maxWidth: "100%",
      });
    } else {
      this.dialog.open(ZoneGroupsDialogComponent, {
        width: "60%",
      });
    }
  }

  openPreferencesDialog(): void {
    this.dialog.open(PreferencesDialogComponent, {
      width: this.mobileChecker.isMobile() ? "90%" : "30%",
    });
  }
}
