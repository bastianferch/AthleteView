import { Component } from '@angular/core';
import { AuthService } from "../../auth/service/auth.service";
import { Router } from "@angular/router";

@Component({
  selector: 'app-main',
  templateUrl: './main.component.html',
  styleUrls: ['./main.component.scss'],
})
export class MainComponent {
  constructor(private authService: AuthService,
    private router: Router) {
  }

  logout(): void {
    this.authService.logout()
    this.router.navigate(['/auth/login'])
  }
}
