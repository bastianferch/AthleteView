import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Step } from "../../dto/Step";
import { EditStepDialogComponent } from "../edit-step-dialog/edit-step-dialog.component";
import { MatDialog } from "@angular/material/dialog"
import { IntervalService } from "../../service/interval.service";
@Component({
  selector: 'app-step',
  templateUrl: './step.component.html',
  styleUrls: ['./step.component.scss', '../../style/interval.scss'],
})
export class StepComponent {
  @Input() step: Step;
  @Input() editable: boolean;
  @Output() deleteStep: EventEmitter<any> = new EventEmitter();
  @Output() changeStep: EventEmitter<Step> = new EventEmitter();

  constructor(public dialog: MatDialog, protected service: IntervalService) {
  }

  openDialog(): void {
    const dialogRef = this.dialog.open(EditStepDialogComponent, {
      data: this.step,
    });

    dialogRef.afterClosed().subscribe((result: Step) => {
      if (result !== undefined) {
        this.changeStep.emit(result);
      }
    });
  }

  onDeleteStep() {
    this.deleteStep.emit();
  }

  onEditStep() {
    this.openDialog();
  }
}
