import { Component, Input, OnInit } from '@angular/core';
import { Activity } from "../../dto/Activity";
import { User } from "../../../user/dto/user";
import { ActivityService } from "../../service/activity.service";
import { UserService } from "../../../user/service/UserService";
import { SnackbarService } from "../../../../common/service/snackbar.service";

// star rating adapted from https://angular-material-star-rating.stackblitz.io
@Component({
  selector: 'app-rating',
  templateUrl: './rating.component.html',
  styleUrls: ['./rating.component.scss'],
})
export class RatingComponent implements OnInit {

  @Input() activity: Activity;

  starCount = 5
  ratingArr: number[] = []
  currentRating = 0

  loggedInUser: User = null

  constructor(
    private activityService: ActivityService,
    private userService: UserService,
    private snackbarService: SnackbarService,
  ) {}

  ngOnInit(): void {
    // get currently logged in user
    this.userService.get().subscribe({
      next: (user) => {
        this.loggedInUser = user
        if (user.isAthlete()) {
          this.currentRating = this.activity.ratingAthlete
        } else {
          this.currentRating = this.activity.ratingTrainer
        }
      },
    });

    for (let index = 0; index < this.starCount; index++) {
      this.ratingArr.push(index);
    }
  }

  onClick(rating:number) {
    this.currentRating = rating;
  }

  showIcon(index:number, rating: number) {
    if (rating >= index + 1) {
      return 'star';
    }
    return 'star_border';

  }

  isAthlete() {
    return this.loggedInUser.isAthlete()
  }

  saveRating() {
    if (this.currentRating === 0) {
      this.snackbarService.openSnackBar("rating must be > 0")
      return;
    }
    this.activityService.rateActivity(this.activity.id, this.currentRating).subscribe({
      next: () => this.snackbarService.openSnackBar("saved rating!"),
      error: (error) => this.snackbarService.openSnackBar(error.error.message),
    })
  }


}
