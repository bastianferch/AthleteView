import { Component, OnInit } from '@angular/core';
import { TrainingsplanService } from "../service/trainingsplan.service";
import { CdkDragDrop, moveItemInArray, transferArrayItem } from "@angular/cdk/drag-drop";
import { ActivityType, Load, PlannedActivity } from "../../activity/dto/PlannedActivity";
import { User } from "../dto/user";
import { forkJoin } from "rxjs";
import { SnackbarService } from "../../../common/service/snackbar.service";

@Component({
  selector: 'app-trainingsplan',
  templateUrl: './trainingsplan.component.html',
  styleUrls: ['./trainingsplan.component.scss'],
})
export class TrainingsplanComponent implements OnInit {

  currentAthlete: User;
  previous: PlannedActivity[];
  upcoming: PlannedActivity[];
  templates: PlannedActivity[];
  athletes:User[];

  sentForScheduling:boolean

  constructor(
    private trainingsplanService: TrainingsplanService,
    private snackbarService:SnackbarService,
  ) {
  }


  ngOnInit(): void {
    this.athletes = []
    this.sentForScheduling = false
    forkJoin([
      this.trainingsplanService.fetchAthletesForTrainer(),
      this.trainingsplanService.fetchPreviousActivitiesForAllAthletes(),
      this.trainingsplanService.fetchUpcomingActivitiesForAllAthletes(),
      this.trainingsplanService.fetchTemplateActivitiesForTrainer(),

    ]).subscribe(([athletes,previousActivities,upcomingActivities,templates]) => {
      this.athletes = athletes
      this.trainingsplanService.updateAthletesInSessionStorage(athletes)
      this.currentAthlete = this.trainingsplanService.getCurrentAthlete()

      this.trainingsplanService.updatePreviousActivitiesInSessionStorage(previousActivities)
      this.previous = this.trainingsplanService.getPreviousActivitiesForCurrentAthlete()

      this.trainingsplanService.updateUpcomingActivities(upcomingActivities)
      this.upcoming = this.trainingsplanService.getUpcomingActivitiesForCurrentAthlete()

      this.trainingsplanService.updateTemplateActivities(templates)
      this.templates = this.trainingsplanService.getTemplateActivities()


    })
  }


  getColor(activity:PlannedActivity):string {
    const colorMapping: { [key in Load]: string } = {
      [Load.EASY]: '#82e010',
      [Load.MEDIUM]: '#f0d807',
      [Load.HARD]: '#de2b0b',
    };
    return colorMapping[activity.load]
  }

  getIconForActivity(activity:PlannedActivity):string {
    const iconMapping: { [key in ActivityType]: string } = {
      [ActivityType.SWIM]: 'swim_icon.png',
      [ActivityType.RUN]: 'run_icon.png',
      [ActivityType.BIKE]: 'bike_icon.png',
      [ActivityType.ROW]: 'row_icon.png',
      [ActivityType.CROSSCOUNTRYSKIING]: 'crosscountryskiing_icon.png',
    };
    return 'assets/activityIcons/' + iconMapping[activity.type]
  }


  sendCSPRequest():void {
    this.trainingsplanService.updateUpcomingActivitiesForAthlete(this.upcoming)
    try {
      this.trainingsplanService.sendCSPRequest().subscribe(
        () => {
          this.snackbarService.openSnackBar("Sent request for scheduling - this may take some time so we will inform you when we're done")
        },
        (error) => {
          this.snackbarService.openSnackBar("Request not sent because: " + error.error.message)
        },
      )
      this.sentForScheduling = true
    } catch (error) {
      if (error instanceof Error) {
        this.snackbarService.openSnackBar("Request not sent because: " + error.message)
      } else {
        this.snackbarService.openSnackBar("Unknown error:" + error)
      }
    }

  }

  updateLists(): void {
    this.previous = this.trainingsplanService.getPreviousActivitiesForCurrentAthlete()
    this.upcoming = this.trainingsplanService.getUpcomingActivitiesForCurrentAthlete()
    this.templates = this.trainingsplanService.getTemplateActivities()
  }

  nextUser(): void {
    this.trainingsplanService.updateUpcomingActivitiesForAthlete(this.upcoming) // "persist" current selection
    this.trainingsplanService.nextAthlete();
    this.currentAthlete = this.trainingsplanService.getCurrentAthlete();
    this.updateLists()
  }

  prevUser(): void {
    this.trainingsplanService.updateUpcomingActivitiesForAthlete(this.upcoming) // "persist" current selection
    this.trainingsplanService.prevAthlete();
    this.currentAthlete = this.trainingsplanService.getCurrentAthlete();
    this.updateLists()
  }

  drop(event: CdkDragDrop<PlannedActivity[]>) {
    if (event.previousContainer === event.container) {
      //  If the item is dropped within the same list, just move the item within the list
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      //  If the item is dropped in a different list, transfer the item to the new list
      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex,
      );
      this.templates = this.trainingsplanService.getTemplateActivities()
      this.trainingsplanService.updateUpcomingActivitiesForAthlete(this.upcoming)
    }
  }
}
