import { NgModule } from '@angular/core';
import { HomeComponent } from '../component/home.component';
import { RouterModule, Routes } from "@angular/router";


const routes: Routes = [
  { path: '', component: HomeComponent },
];

@NgModule({
  declarations: [
    HomeComponent,
  ],
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class HomeModule { }
