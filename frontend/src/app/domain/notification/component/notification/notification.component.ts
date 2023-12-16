import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NotificationDto } from "../../dto/notification-dto";

@Component({
  selector: 'app-notification',
  templateUrl: './notification.component.html',
  styleUrls: ['./notification.component.scss'],
})
export class NotificationComponent {

  @Input() notification: NotificationDto
  @Output() deleteNotification = new EventEmitter<number>()
  @Output() navigateTo = new EventEmitter<string>()

  // returns a string representation of the notifications timestamp (as a localized date string)
  // instead of today's or yesterday's date, it returns "today" or "yesterday".
  // exact time is only returned if timestamp is from today or yesterday.
  getTimestampString() {
    const date = new Date(this.notification.timestamp);
    const now = new Date();

    let dateString = "";
    // format: "hh:mm"
    let timeString = new Intl.DateTimeFormat(
      undefined,
      { hour: '2-digit', minute: '2-digit', hour12: false },
    ).format(date);

    if (now.getDate() === date.getDate()
      && now.getMonth() === date.getMonth()
      && now.getFullYear() === date.getFullYear()) {
      dateString = "today";
    } else if (now.getDate() === date.getDate() + 1
      && now.getMonth() === date.getMonth()
      && now.getFullYear() === date.getFullYear()) {
      dateString = "yesterday";
    } else {
      dateString = new Intl.DateTimeFormat().format(date);
      timeString = "";
    }

    return (`${dateString}${timeString === '' ? '' : ', ' + timeString}`)
  }

  delete() {
    this.deleteNotification.emit(this.notification.id)
  }

  navigateLink() {
    if (this.notification.link) {
      this.navigateTo.emit(this.notification.link)
    }
  }
}
