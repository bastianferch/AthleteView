import { Component, OnInit } from '@angular/core';
import { NotificationService } from "../../service/notification.service";

@Component({
  selector: 'app-notification-list',
  templateUrl: './notification-list.component.html',
  styleUrls: ['./notification-list.component.scss'],
})
export class NotificationListComponent implements OnInit {

  constructor(protected notificationService: NotificationService) {
  }

  deleteNotification(id: number) {
    this.notificationService.deleteNotification(id);
  }

  navigateTo(route: string) {
    this.notificationService.navigateTo(route);
  }

  deleteAllNotifications() {
    this.notificationService.deleteAllNotifications();
  }

  ngOnInit(): void {
    this.notificationService.createEventSource();
    this.notificationService.getAllNotifications();
  }


}
