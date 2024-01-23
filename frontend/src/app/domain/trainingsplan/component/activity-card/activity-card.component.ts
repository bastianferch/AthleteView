import {Component, EventEmitter, Input, Output} from '@angular/core';
import {PlannedActivity} from "../../../activity/dto/PlannedActivity";
import {StyleMapperService} from "../../../../common/service/style-mapper.service";

@Component({
  selector: 'app-activity-card',
  template: `
    <mat-card class="activity-card">
      <mat-card-content class="activity-card-content">
        <div class="colored-bar" [style.background-color]="getColorCode(activity)"></div>
        <img class="preview"
             [src]="getIconPath(activity)"
             alt="seife"
        />
        <div>
          <div>{{ activity.name }} </div>
          <div>{{activity.estimatedDuration}}</div>
          <div>{{ activity.note}}</div>
        </div>
        <div>
          <div *ngIf="activity.template">
            <div>
              <a>With Trainer?
                <mat-checkbox class="checkbox"
                              [(ngModel)]="activity.withTrainer"
                              [disabled] = "!interActive"
                              (change)="onCheckboxChange()"
                              checked="{{activity.withTrainer}}">

                </mat-checkbox>
              </a>
            </div>
          </div>
        </div>
        <div *ngIf="activity.template && interActive">
          <svg width="24px" fill="currentColor" viewBox="0 0 24 24">
            <path
              d="M10 9h4V6h3l-5-5-5 5h3v3zm-1 1H6V7l-5 5 5 5v-3h3v-4zm14 2l-5-5v3h-3v4h3v3l5-5zm-9 3h-4v3H7l5 5 5-5h-3v-3z"></path>
            <path d="M0 0h24v24H0z" fill="none"></path>
          </svg>
        </div>
      </mat-card-content>
    </mat-card>
  `,
  styleUrls: ['./activity-card.component.scss']
})


export class ActivityCardComponent {
  @Input() activity: PlannedActivity;
  @Input() interActive: boolean = false;

  @Output() objectChanged: EventEmitter<PlannedActivity> = new EventEmitter<PlannedActivity>();

  constructor(
    private styleMapperService: StyleMapperService
  ) {
  }
  onCheckboxChange(): void {
    this.objectChanged.emit(this.activity);
  }

  getColorCode(activity:PlannedActivity):string{
    return this.styleMapperService.getIntensityColor(activity)
  }

  getIconPath(activity:PlannedActivity):string{
    return this.styleMapperService.getIconPathForActivity(activity)
  }
}
