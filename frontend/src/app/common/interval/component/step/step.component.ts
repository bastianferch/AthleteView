import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Step } from "../../dto/Step";

@Component({
  selector: 'app-step',
  templateUrl: './step.component.html',
  styleUrls: ['./step.component.scss'],
})
export class StepComponent {
  @Input() step: Step;
  @Output() deleteStep: EventEmitter<any> = new EventEmitter();

  onDeleteStep() {
    this.deleteStep.emit();
  }

  onEditStep() {
    console.log("edit step")
  }
}
