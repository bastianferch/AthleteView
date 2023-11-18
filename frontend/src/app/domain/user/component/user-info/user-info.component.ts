import { Component } from '@angular/core';
import { UserService } from "../../service/UserService";

@Component({
  selector: 'app-user-info',
  templateUrl: './user-info.component.html',
  styleUrls: ['./user-info.component.scss'],
})
export class UserInfoComponent {
  constructor(private userService: UserService) {
  }

}
