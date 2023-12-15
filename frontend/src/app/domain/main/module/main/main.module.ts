import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MainComponent } from '../../component/main.component';
import { RouterLink, RouterOutlet } from "@angular/router";
import { SharedModule } from "../../../../config/module/SharedModule";
import { MatToolbarModule } from "@angular/material/toolbar";
import { MatMenuModule } from "@angular/material/menu";


@NgModule({
  declarations: [
    MainComponent,
  ],
  imports: [
    CommonModule,
    RouterOutlet,
    SharedModule,
    RouterLink,
    MatToolbarModule,
    MatMenuModule,
  ],
  exports: [
    MainComponent,
  ],
})
export class MainModule { }
