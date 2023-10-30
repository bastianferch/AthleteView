import { Component, OnInit } from '@angular/core';
import { UserService } from "../../service/UserService";
import { firstValueFrom } from "rxjs";
import { User } from "../../dto/User";

@Component({
  selector: 'app-user-info',
  templateUrl: './user-info.component.html',
  styleUrls: ['./user-info.component.scss'],
})
export class UserInfoComponent implements OnInit {
  testUser: User;
  isBackendError: boolean;
  constructor(private userService: UserService) {
  }

  ngOnInit(): void {
    this.createTestData();
  }

  async createTestData(): Promise<void> {
    this.isBackendError = false;
    this.testUser = await firstValueFrom(this.userService.createTestData()).catch((err) => {
      this.isBackendError = true;
      return null;
    });
  }

}
