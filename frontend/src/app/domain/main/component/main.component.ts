import { Component } from '@angular/core';
import { AuthService } from "../../auth/service/auth.service";
import { Router } from "@angular/router";
import { MatDialog } from "@angular/material/dialog";
import { FitImportDialogComponent } from 'src/app/fit-import-dialog/fit-import-dialog.component';

@Component({
  selector: 'app-main',
  templateUrl: './main.component.html',
  styleUrls: ['./main.component.scss']
})
export class MainComponent {
  constructor(private authService: AuthService,
    private router: Router,
    private dialog: MatDialog) {
  }
  logout(): void {
    this.authService.logout()
    this.router.navigate(['/auth/login'])
  }

  openImportDialog(): void {
    let dialogRef = this.dialog.open(FitImportDialogComponent, {
      width: "30%",
      data: {}
    });

    dialogRef.afterClosed().subscribe((data) => {
      console.log(`dialog return value ${data}`);
    });
  }
}
