import { Component, OnInit } from '@angular/core';
import { TrainingsplanService } from "../service/trainingsplan.service";
import { CdkDragDrop, moveItemInArray, transferArrayItem } from "@angular/cdk/drag-drop";
import { PlannedActivity } from "../../activity/dto/PlannedActivity";
import { User } from "../dto/user";
import { forkJoin } from "rxjs";
import { SnackbarService } from "../../../common/service/snackbar.service";
import { MatDialog } from '@angular/material/dialog';
import { ModalComponent } from "./modal/modal.component";

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
  jobExisting:boolean

  interactive:boolean

  constructor(
    private trainingsplanService: TrainingsplanService,
    private snackbarService:SnackbarService,
    public dialog: MatDialog,
  ) {
  }


  ngOnInit(): void {
    this.athletes = []
    this.sentForScheduling = false
    this.interactive = true
    forkJoin([
      this.trainingsplanService.fetchAthletesForTrainer(),
      this.trainingsplanService.fetchPreviousActivitiesForAllAthletes(),
      this.trainingsplanService.fetchUpcomingActivitiesForAllAthletes(),
      this.trainingsplanService.fetchTemplateActivitiesForTrainer(),
      this.trainingsplanService.fetchJobExists(),
    ]).subscribe(([athletes,previousActivities,upcomingActivities,templates,jobExisting]) => {
      this.athletes = athletes
      this.trainingsplanService.updateAthletesInSessionStorage(athletes)
      this.currentAthlete = this.trainingsplanService.getCurrentAthlete()

      this.trainingsplanService.updatePreviousActivitiesInSessionStorage(previousActivities)
      this.previous = this.trainingsplanService.getPreviousActivitiesForCurrentAthlete()

      this.trainingsplanService.updateUpcomingActivities(upcomingActivities)
      this.upcoming = this.trainingsplanService.getUpcomingActivitiesForCurrentAthlete()

      this.trainingsplanService.updateTemplateActivities(templates)
      this.templates = this.trainingsplanService.getTemplateActivities()

      this.jobExisting = jobExisting
      if (this.jobExisting) {
        this.openModal()
      }
    })
  }

  openModal():void {
    const dialogRef = this.dialog.open(ModalComponent, {
      width: '400px',
      data: { title: 'Trainingsplan Exisits', content: 'A trainingsplan for next week has already been created. Do you want to:',option1: "Reset",option2: "Look at the plan" },
      disableClose: true,
    })

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.trainingsplanService.sendJobDeleteRequest().subscribe(
          () => {
            this.snackbarService.openSnackBar("Previous Trainingsplan deleted.")
          },
          (error) => {
            this.snackbarService.openSnackBar("Could not delete previous Trainingsplan because: " + error.error.message)
          },
        )
      } else {
        // TODO: set non interactive here
        this.sentForScheduling = true;
        this.interactive = false
      }
    });
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
