import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MainComponent } from '../../component/main.component';
import { RouterLink, RouterOutlet } from "@angular/router";
import { SharedModule } from "../../../../config/module/SharedModule";


@NgModule({
  declarations: [
    MainComponent,
  ],
  imports: [
    CommonModule,
    RouterOutlet,
    SharedModule,
    RouterLink,
  ],
  exports: [
    MainComponent,
  ],
})
export class MainModule { }