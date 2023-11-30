import { Component, OnDestroy, OnInit } from '@angular/core';
import { interval, Subscription } from "rxjs";
import { ActivatedRoute, Router } from "@angular/router";
import { AuthService } from "../../service/auth.service";

@Component({
  selector: 'app-confirm-registration',
  templateUrl: './confirm-registration.component.html',
  styleUrls: ['./confirm-registration.component.scss'],
})
export class ConfirmRegistrationComponent implements OnDestroy, OnInit {
  private TOTAL_TIME = 15;
  private countDown: Subscription;
  public secondsLeft: number;

  uuid: string;
  isError: boolean;
  success = false;

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService,
    private router: Router) {
  }

  ngOnInit(): void {
    // start timer
    const fixed = new Date();
    this.countDown = interval(1000).subscribe(
      () => {
        const delta = new Date().getTime() - fixed.getTime();
        this.secondsLeft = this.TOTAL_TIME - Math.floor(delta / 1000 % 60);

        if (this.secondsLeft === 0) {
          this.router.navigateByUrl('/');
        }
      },
    );

    // check confirmation token
    this.route.params.subscribe((params) => {
      this.uuid = params['uuid'];
      this.authService.confirmAccount(this.uuid).subscribe({
        next: () => {
          this.success = true;
        },
        error: () => this.isError = true,
      });
    });
  }

  ngOnDestroy(): void {
    this.countDown.unsubscribe();
  }
}
