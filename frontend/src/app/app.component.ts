import { Component, OnDestroy, OnInit } from '@angular/core';
import { AuthService } from "./domain/auth/service/auth.service";
import { Subject, takeUntil } from "rxjs";
import { User } from "./domain/user/dto/user";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit, OnDestroy {
  private _destroyStream: Subject<void> = new Subject<void>();
  currentUser: User;
  constructor(public authService: AuthService) {
  }

  ngOnInit(): void {
    this.authService.checkCurrentUser().then()
    this.authService.getCurrentUser$
      .pipe(takeUntil(this._destroyStream))
      .subscribe((user) => {
        this.currentUser = user;
      });
  }

  ngOnDestroy(): void {
    this._destroyStream.next();
  }
}
