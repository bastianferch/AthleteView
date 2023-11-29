import { ChangeDetectorRef, Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ActivityNameMapper, ActivityType, convertToPlannedActivity, convertToPlannedActivitySplit, PlannedActivity } from '../../dto/PlannedActivity';
import { Interval } from '../../../../common/interval/dto/Interval';
import { ActivityService } from '../../service/activity.service';
import { SnackbarService } from '../../../../common/service/snackbar.service';
import { IntervalContainerComponent } from "../../../../common/interval/component/interval-container/interval-container.component";
import { Location } from '@angular/common';
import { HttpStatusCode } from "@angular/common/http";

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

  @ViewChild(IntervalContainerComponent) intervalComponent: IntervalContainerComponent;

  protected readonly ActivityCreateEditViewMode = ActivityCreateEditViewMode;
  public activityTypes = Object.values(ActivityType);

  protected activityMapper
  plannedActivity: PlannedActivity = undefined;

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

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private activityService: ActivityService,
    private snackbarService: SnackbarService,
    private changeDetector: ChangeDetectorRef,
    private location: Location) {
    this.activityMapper = ActivityNameMapper;
  }

  ngOnInit(): void {
    this.route.data.subscribe((data) => {
      this.mode = data['mode'];
      this.changeDetector.detectChanges()
      if (this.mode !== ActivityCreateEditViewMode.create) {
        this.activityService.getPlannedActivity(this.route.snapshot.params['id']).subscribe((activity) => {
          this.plannedActivity = convertToPlannedActivity(activity);
          this.intervalComponent.manuallyLoadInterval(this.plannedActivity.interval);
        },
        (error) => {
          if (error.status === HttpStatusCode.NotFound) {
            this.snackbarService.openSnackBar("Activity not found.")
            this.redirectToHome();
          }
        });
      } else {
        // if this is the create screen, initialize the activity with a default one.
        this.plannedActivity = {
          id: null,
          interval: undefined,
          template: false,
          type: ActivityType.RUN,
          withTrainer: false,
          note: "",
          date: null,
          createdBy: null, // stays. Will be ignored by the backend anyway
          createdFor: null,
        }
      }
    });
  }

  // TODO add routing after save (after other components are done)
  save(): void {
    let planned = convertToPlannedActivitySplit(this.plannedActivity);
    planned = this.activityService.postProcessActivity(planned);

    const isValid = this.activityService.validateActivity(planned)
    if (!isValid) {
      return;
    }

    if (planned !== undefined) {
      if (this.mode === ActivityCreateEditViewMode.create) {
        this.activityService.createPlannedActivity(planned).subscribe({
          next: (savedActivity) => {
            this.snackbarService.openSnackBar("Activity successfully created ")
            // redirect to the newly created activity
            this.redirectToActivity(savedActivity.id);
          },
          error: (err) => {
            this.snackbarService.openSnackBar("Activity creation failed with " + err?.error?.message)
          },
        });
      } else if (this.mode === ActivityCreateEditViewMode.edit) {
        this.activityService.editPlannedActivity(planned).subscribe({
          next: () => this.snackbarService.openSnackBar("Activity successfully edited "),
          error: (err) => this.snackbarService.openSnackBar("Activity edit failed with " + err?.error?.message),
        })
      }
    } else {
      this.snackbarService.openSnackBar("Activity creation failed \n Check all input fields")
    }
  }

  handleChange(interval: Interval) {
    this.plannedActivity.interval = interval;
  }

  redirectBack() {
    // TODO check if we want to navigate somewhere else
    //  and maybe do some cleanup?
    this.location.back();
  }

  redirectToActivity(id: number) {
    // TODO check if we want to navigate somewhere else
    //  and maybe do some cleanup?
    this.router.navigateByUrl(`/activity/${id}`).then();
  }

  redirectToHome() {
    this.router.navigateByUrl("/").then();
  }
}
