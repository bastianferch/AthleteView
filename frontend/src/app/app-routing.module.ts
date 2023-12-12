import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { authGuard } from "./config/auth.guard";
import { LegalInformationComponent } from "./domain/user/component/legal-information/legal-information.component";

const routes: Routes = [
  {
    path: '', canActivate: [authGuard],
    loadChildren: () => import('src/app/domain/home/module/home.module').then((m) => m.HomeModule),
  },
  {
    path: 'activity', canActivate: [authGuard], // TODO bugfix for not redirecting to login page
    loadChildren: () => import('src/app/domain/activity/module/activity.module').then((m) => m.ActivityModule),
  },
  {
    path: 'calendar', canActivate: [authGuard],
    loadChildren: () => import('src/app/domain/calendar/module/custom-calendar.module').then((m) => m.CustomCalendarModule),
  },
  { path: 'time-constraints', canActivate: [authGuard],
    loadChildren: () => import('src/app/domain/time-constraints/module/time-constraints.module').then((m) => m.TimeConstraintsModule) },
  { path: 'profile', canActivate: [authGuard],
    loadChildren: () => import('src/app/domain/user/UserModule').then((m) => m.UserModule) },
  { path: 'auth',
    loadChildren: () => import('src/app/domain/auth/module/auth.module').then((m) => m.AuthModule) },
  { path: 'legal', component: LegalInformationComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule { }
