import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { authGuard } from "./config/auth.guard";
import { LegalInformationComponent } from "./domain/user/component/legal-information/legal-information.component";

const routes: Routes = [
  { path: '', canActivate: [authGuard],
    loadChildren: () => import('src/app/domain/home/module/home.module').then((m) => m.HomeModule) },
  { path: 'activities', canActivate: [authGuard],
    loadChildren: () => import('src/app/domain/activities/module/activities.module').then((m) => m.ActivitiesModule) },
  { path: 'calendar', canActivate: [authGuard],
    loadChildren: () => import('src/app/domain/calendar/module/calendar.module').then((m) => m.CalendarViewModule) },
  { path: 'auth',
    loadChildren: () => import('src/app/domain/auth/module/auth.module').then((m) => m.AuthModule) },
  { path: 'legal', component: LegalInformationComponent },
  { path: 'time-constraints', canActivate: [authGuard],
    loadChildren: () => import('src/app/domain/time-constraints/module/time-constraints.module').then((m) => m.TimeConstraintsModule) },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule { }
