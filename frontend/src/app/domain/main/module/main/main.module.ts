import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MainComponent } from '../../component/main.component';
import { RouterLink, RouterOutlet } from "@angular/router";
import { SharedModule } from "../../../../config/module/SharedModule";
import { MatToolbarModule } from "@angular/material/toolbar";
import { MatMenuModule } from "@angular/material/menu";

import { MatSidenavModule } from "@angular/material/sidenav";
import { NotificationComponent } from '../../../notification/component/notification/notification.component';
import { NotificationListComponent } from '../../../notification/component/notification-list/notification-list.component';
import { MatCardModule } from "@angular/material/card";
import { MatBadgeModule } from "@angular/material/badge";

@NgModule({
  declarations: [
    MainComponent,
    NotificationComponent,
    NotificationListComponent,
  ],
  imports: [
    CommonModule,
    RouterOutlet,
    SharedModule,
    RouterLink,
    MatSidenavModule,
    MatCardModule,
    MatBadgeModule,
    MatToolbarModule,
    MatMenuModule,
  ],
  exports: [
    MainComponent,
  ],
})
export class MainModule { }
