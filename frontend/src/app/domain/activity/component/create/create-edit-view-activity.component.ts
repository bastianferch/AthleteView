import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ActivityType, PlannedActivity } from '../../../../common/dto/PlannedActivity';
import { Interval } from '../../../../common/interval/dto/Interval';
import { ActivityService } from '../../service/activity.service';

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

  plannedActivity: PlannedActivity = {
    id: undefined, interval: undefined, template: false, type: undefined, with_trainer: false, note: undefined, date: undefined,
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

  constructor(private route: ActivatedRoute, private activityService: ActivityService) {
  }

  ngOnInit(): void {
    this.route.data.subscribe((data) => {
      this.mode = data['mode'];
    });
  }

  save(): void {

    if (this.mode === ActivityCreateEditViewMode.create) {

      this.activityService.createPlannedActivity(this.plannedActivity);
    } else if (this.mode === ActivityCreateEditViewMode.edit) {
      this.activityService.editPlannedActivity(this.plannedActivity);
    }
  }

  handleChange(interval: Interval) {
    this.plannedActivity.interval = interval;
  }
}
