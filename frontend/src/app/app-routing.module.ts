import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { authGuard } from "./config/auth.guard";

const routes: Routes = [
  { path: '', canActivate: [authGuard],
    loadChildren: () => import('src/app/domain/home/module/home.module').then((m) => m.HomeModule) },
  { path: 'activity', // TODO add authGuard
    loadChildren: () => import('src/app/domain/activity/module/activity.module').then((m) => m.ActivityModule) },
  { path: 'calendar', canActivate: [authGuard],
    loadChildren: () => import('src/app/domain/calendar/module/calendar.module').then((m) => m.CalendarModule) },
  { path: 'auth',
    loadChildren: () => import('src/app/domain/auth/module/auth.module').then((m) => m.AuthModule) },
  // TODO this is just for testing the component. remove later.
  { path: 'intervalTest',
    loadChildren: () => import('src/app/common/interval/IntervalModule').then((m) => m.IntervalModule) },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule { }
