import { ChangeDetectorRef, Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ActivityNameMapper, ActivityType, convertToPlannedActivity, convertToPlannedActivitySplit, PlannedActivity } from '../../dto/PlannedActivity';
import { Interval } from '../../../../common/interval/dto/Interval';
import { ActivityService } from '../../service/activity.service';
import { SnackbarService } from '../../../../common/service/snackbar.service';
import { IntervalContainerComponent } from "../../../../common/interval/component/interval-container/interval-container.component";
import { Location } from '@angular/common';
import { HttpStatusCode } from "@angular/common/http";
import { UserService } from "../../../user/service/UserService";
import { User } from '../../../user/dto/user';
import { AuthService } from "../../../auth/service/auth.service";

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
  athletes: User[] = undefined;
  currentUser: User = undefined;

  mode: ActivityCreateEditViewMode;

  public get heading(): string {
    switch (this.mode) {
      case ActivityCreateEditViewMode.create:
        return 'Create Activity';
      case ActivityCreateEditViewMode.edit:
        return 'Edit Activity';
      case ActivityCreateEditViewMode.details:
        return this.plannedActivity.name
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
    private location: Location,
    private userService: UserService,
    private authService: AuthService) {
    this.activityMapper = ActivityNameMapper;
    this.currentUser = this.authService.currentUser;
  }

  ngOnInit(): void {
    this.route.data.subscribe((data) => {
      this.mode = data['mode'];
      this.changeDetector.detectChanges()
      if (this.mode !== ActivityCreateEditViewMode.create) {
        this.activityService.getPlannedActivity(this.route.snapshot.params['id']).subscribe((activity) => {
          this.plannedActivity = convertToPlannedActivity(activity);
          if (this.plannedActivity.date !== null) {
            this.plannedActivity.date = this.parseDate(this.plannedActivity.date as number[])
          }
          if (this.mode === ActivityCreateEditViewMode.details && !this.plannedActivity.template) {
            this.athletes = [this.plannedActivity.createdFor]
          }
        },
        (error) => {
          if (error.status === HttpStatusCode.NotFound) {
            this.snackbarService.openSnackBar("Activity not found.")
            this.redirectToHome();
          }
        });
      } else {
        // if this is the create screen, initialize the activity with a default one.
        this.currentUser = this.authService.currentUser
        this.plannedActivity = {
          id: null,
          name: "default activity",
          interval: undefined,
          template: false,
          type: ActivityType.RUN,
          withTrainer: false,
          note: "",
          date: new Date(),
          estimatedDuration: 120,
          load: undefined,
          createdBy: null, // stays. Will be ignored by the backend anyway
          createdFor: null,
          activity: null,
        }
      }
      if (this.mode !== ActivityCreateEditViewMode.details) {
        this.loadAthletes()
      }
    });
  }

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
          next: () => {
            this.snackbarService.openSnackBar("Activity successfully edited ")
            this.redirectToActivity(planned.id);
          },
          error: (err) => this.snackbarService.openSnackBar("Activity edit failed with " + err?.error?.message),
        })
      }
    } else {
      this.snackbarService.openSnackBar("Activity creation failed \n Check all input fields")
    }
  }

  loadAthletes() {
    if (!this.currentUser.isAthlete() && this.athletes === undefined) {
      this.userService.fetchAthletesForTrainer().subscribe({
        next: (athletes) => {
          this.athletes = athletes;
          // somehow the createdFor is not recognized as the same object
          this.athletes = this.athletes.map((athlete) =>
            (athlete.id === this.plannedActivity.createdFor?.id ? this.plannedActivity.createdFor : athlete),
          );
        },
        error: (err) => {
          this.snackbarService.openSnackBar("Failed to fetch athletes for trainer: " + err?.error?.message)
        },
      })
    }
  }

  handleChange(interval: Interval) {
    this.plannedActivity.interval = interval;
  }

  redirectBack() {
    this.location.back();
  }

  redirectToActivity(id: number) {
    this.router.navigateByUrl(`/activity/${id}`).then();
  }

  redirectToHome() {
    this.router.navigateByUrl("/").then();
  }

  parseDate(numbers: number[]): any {
    const str: string[] = []
    numbers.forEach((num) => {
      if (num.toString().length === 1) {
        str.push("0" + num.toString())
      } else {
        str.push(num.toString())
      }
    });
    return str[0] + "-" + str[1] + "-" + str[2] + "T" + str[3] + ":" + str[4]
  }
}
