import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { authGuard } from "./config/auth.guard";

const routes: Routes = [
  { path: '', canActivate: [authGuard],
    loadChildren: () => import('src/app/domain/user/UserModule').then((m) => m.UserModule) },
  { path: 'auth',
    loadChildren: () => import('src/app/domain/auth/module/auth.module').then((m) => m.AuthModule) },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule { }
