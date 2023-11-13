import { Component, Input } from '@angular/core';
import { Step } from "../../dto/Step";

@Component({
  selector: 'app-step',
  templateUrl: './step.component.html',
  styleUrls: ['./step.component.scss'],
})
export class StepComponent {
  @Input() step: Step;

}
