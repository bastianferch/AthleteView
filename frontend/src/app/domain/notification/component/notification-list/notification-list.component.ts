import { Component, OnInit } from '@angular/core';
import { NotificationService } from '../../service/notification.service';
import { UserService } from '../../../user/service/UserService';
import { SnackbarService } from '../../../../common/service/snackbar.service';
import { NotificationDto } from "../../dto/notification-dto";

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

  navigateOrPerformAction(route: NotificationDto) {
    if (route.link.startsWith('action/')) {
      this.performAction(route.link.substring(7));
      this.deleteNotification(route.id)
    } else {
      this.notificationService.navigateTo(route.link);
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
