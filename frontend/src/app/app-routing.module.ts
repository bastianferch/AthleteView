import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { authGuard } from "./config/auth.guard";

const routes: Routes = [
  { path: '', canActivate: [authGuard],
    loadChildren: () => import('src/app/domain/home/module/home.module').then((m) => m.HomeModule) },
  { path: 'activity', canActivate: [authGuard],
    loadChildren: () => import('src/app/domain/activity/module/activity.module').then((m) => m.ActivityModule) },
  { path: 'calendar', canActivate: [authGuard],
    loadChildren: () => import('src/app/domain/calendar/module/calendar.module').then((m) => m.CalendarModule) },
  { path: 'auth',
    loadChildren: () => import('src/app/domain/auth/module/auth.module').then((m) => m.AuthModule) },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule { }
