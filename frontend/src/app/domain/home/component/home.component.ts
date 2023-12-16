import { Component } from '@angular/core';
import { NotificationService } from "../../notification/service/notification.service";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
})
export class HomeComponent {

  constructor(private notificationService: NotificationService) {
  }

  testPostNotification() {
    this.notificationService.testPostNotification();
  }

}
