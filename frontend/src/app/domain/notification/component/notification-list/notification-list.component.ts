import { Component, OnInit } from '@angular/core';
import { NotificationService } from '../../service/notification.service';
import { UserService } from '../../../user/service/UserService';
import { SnackbarService } from '../../../../common/service/snackbar.service';

@Component({
  selector: 'app-notification-list',
  templateUrl: './notification-list.component.html',
  styleUrls: ['./notification-list.component.scss'],
})
export class NotificationListComponent implements OnInit {

  constructor(protected notificationService: NotificationService, private snackbarService: SnackbarService, private userService: UserService) {
  }

  deleteNotification(id: number) {
    this.notificationService.deleteNotification(id);
  }

  navigateOrPerformAction(route: string) {
    if (route.startsWith('action/')) {
      this.performAction(route.substring(7));
    } else {
      this.notificationService.navigateTo(route);
    }

  }

  performAction(route: string) {
    if (route.startsWith('acceptAthlete/')) {
      this.accept(Number(route.substring(14)))

    }
  }


  accept(id: number) {
    this.userService.acceptAthlete(id).subscribe({
      next: () => {
        this.snackbarService.openSnackBar('Athlete successfully accepted')
      },
      error: (err) => {
        this.snackbarService.openSnackBar('Failed to accept athlete: ' + err?.error?.message)
      },
    })
  }


  deleteAllNotifications() {
    this.notificationService.deleteAllNotifications();
  }

  ngOnInit(): void {
    this.notificationService.createEventSource();
    this.notificationService.getAllNotifications();
  }


}
