import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { NotificationDto } from "../../dto/notification-dto";
import { DateParsing } from "../../../../common/util/parsing/date-parsing";

@Component({
  selector: 'app-notification',
  templateUrl: './notification.component.html',
  styleUrls: ['./notification.component.scss'],
})
export class NotificationComponent implements OnInit {

  @Input() notification: NotificationDto
  @Output() deleteNotification = new EventEmitter<number>()
  @Output() navigateTo = new EventEmitter<NotificationDto>()

  actionName: string;

  constructor(
    private dateParser: DateParsing,
  ) {}

  ngOnInit() {
    if (this.notification.link.startsWith('action/')) {
      this.setActionName(this.notification.link.substring(7));
    }
  }

  getTimestampString() {
    return this.dateParser.getDateAwareString(this.notification.timestamp)
  }

  delete() {
    this.deleteNotification.emit(this.notification.id)
  }

  navigateLink() {
    if (this.notification.link) {
      this.navigateTo.emit(this.notification)
    }
  }

  setActionName(link: string) {
    if (link.startsWith('acceptAthlete/')) {
      this.actionName = 'Accept';
    }
  }
}
