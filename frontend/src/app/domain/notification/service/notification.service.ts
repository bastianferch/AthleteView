import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { UrlService } from "../../../config/service/UrlService";
import { AuthService } from "../../auth/service/auth.service";
import { EventSourcePolyfill } from 'event-source-polyfill';
import { NotificationDto } from "../dto/notification-dto";
import { MatDrawer } from "@angular/material/sidenav";
import { Router } from "@angular/router";
import * as _ from "lodash";

@Injectable({
  providedIn: 'root',
})
export class NotificationService {

  public readonly url: string;
  public notifications: NotificationDto[] = [];
  public notificationDrawer: MatDrawer = null;
  private eventSource: EventSourcePolyfill;

  constructor(
    private http: HttpClient,
    private urlService: UrlService,
    private authService: AuthService,
    private router: Router,
  ) {
    this.url = this.urlService.getBackendUrl() + 'notification';
  }

  createEventSource() {

    // if there is already an event source, close the old one
    if (this.eventSource !== null && this.eventSource !== undefined) {
      this.eventSource.close()
    }

    // standard EventSource cannot set headers. Use custom EventSourcePolyfill instead
    // Note: These requests do *not* pass through the http interceptors.
    //       Therefore, authentication headers have to be set here
    this.eventSource = new EventSourcePolyfill(this.url + "/subscribe", {
      withCredentials: true,
      heartbeatTimeout: 18000000, // since the connection is kept open anyway, no heartbeat is required. set the timeout to 5h
      headers: {
        'Authorization': `Bearer ${this.authService.getAuthToken()}`,
      },
    });

    this.eventSource.onmessage = (event) => {
      const notification : NotificationDto = JSON.parse(event.data);
      this.notifications.push(notification);
      this.sortNotifications();
    };
    this.eventSource.onerror = () => {
      // upon encountering an error (when the backend closes the connection), close the connection client-side as well.
      this.eventSource.close();
    }
  }

  getAllNotifications() {
    return this.http.get<NotificationDto[]>(
      this.url,
      { withCredentials: true },
    ).subscribe((data) => {
      this.notifications = data
      this.sortNotifications();
    })
  }

  deleteNotification(id: number) {
    this.notifications = this.notifications.filter((n) => n.id !== id);
    return this.http.delete(
      this.url + "/" + id,
      { withCredentials: true },
    ).subscribe();
  }

  deleteAllNotifications() {
    this.notifications = [];
    this.http.delete(
      this.url,
      { withCredentials: true },
    ).subscribe();
    this.notificationDrawer.close();
  }

  markAllNotificationsAsRead() {
    // if there are no unread notifications, do nothing
    if (this.notifications.filter((n) => n.read === false).length === 0) {
      return;
    }
    this.notifications = this.notifications.map((n) => Object.assign(n, { read: true }));
    this.http.patch(
      this.url,
      null,
      { withCredentials: true },
    ).subscribe();
  }

  clearNotifications() {
    this.notifications = [];
  }

  navigateTo(route: string) {
    this.notificationDrawer.close();
    this.router.navigateByUrl(route);
  }

  private sortNotifications() {
    this.notifications = _.uniqBy(this.notifications, (n: NotificationDto) => n.id)
    this.notifications.sort((a, b) => b.timestamp - a.timestamp);
  }
}
