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
  constructor(private userService: UserService) {
  }

  ngOnInit(): void {
  }

}
