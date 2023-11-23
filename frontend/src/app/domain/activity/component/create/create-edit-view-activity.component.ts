import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ActivityType, convertToPlannedActivitySplit, PlannedActivity } from '../dto/PlannedActivity';
import { Interval } from '../../../../common/interval/dto/Interval';
import { ActivityService } from '../../service/activity.service';
import { SnackbarService } from '../../../../common/service/snackbar.service';
import { User } from "../../../user/dto/User";

export enum ActivityCreateEditViewMode {
  create,
  edit,
  details
}

@Component({
  selector: 'app-create-edit-view-component',
  templateUrl: './create-edit-view-activity.component.html',
  styleUrls: ['./create-edit-view-activity.component.scss'],
})
export class CreateEditViewActivityComponent implements OnInit {


  public activityTypes = Object.values(ActivityType);

  currentUser: User = new User();


  plannedActivity: PlannedActivity = {
    id: null,
    interval: undefined,
    template: false,
    type: ActivityType.RUN,
    withTrainer: false,
    note: "",
    date: null,
    createdBy: null,
    createdFor: null,
  }

  mode: ActivityCreateEditViewMode;

  public get heading(): string {
    switch (this.mode) {
      case ActivityCreateEditViewMode.create:
        return 'Create Activity';
      case ActivityCreateEditViewMode.edit:
        return 'Edit Activity';
      case ActivityCreateEditViewMode.details:
        return 'Activity name';
      default:
        return '?';
    }
  }

  constructor(private route: ActivatedRoute, private activityService: ActivityService, private snackbarService: SnackbarService) {
  }

  ngOnInit(): void {
    this.route.data.subscribe((data) => {
      this.mode = data['mode'];
    });
    this.setUser();
  }

  save(): void {
    const planned = convertToPlannedActivitySplit(this.plannedActivity);
    if (planned !== undefined) {
      if (this.mode === ActivityCreateEditViewMode.create) {
        this.activityService.createPlannedActivity(planned).subscribe({
          next: () => this.snackbarService.openSnackBar("Activity successfully created "),
          error: (err) => this.snackbarService.openSnackBar("Activity creation failed with " + err.message),
        });
      } else if (this.mode === ActivityCreateEditViewMode.edit) {
        this.activityService.editPlannedActivity(planned);
      }
    } else {
      this.snackbarService.openSnackBar("Activity creation failed \n Check all input fields")
    }


  }

  handleChange(interval: Interval) {
    this.plannedActivity.interval = interval;
  }

  private setUser() {
    this.currentUser.id = parseInt(localStorage.getItem("user_id"), 10);
    this.currentUser.name = "";
    this.currentUser.email = "";
    this.plannedActivity.createdBy = this.currentUser;

  }
}
