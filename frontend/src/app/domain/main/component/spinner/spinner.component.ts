import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Subject, takeUntil } from "rxjs";
import { SpinnerService } from "../../service/spinner.service";

@Component({
  selector: 'app-spinner',
  templateUrl: './spinner.component.html',
  styleUrls: ['./spinner.component.scss'],
})
export class SpinnerComponent implements OnInit, OnDestroy {
  private _destroyStream: Subject<void> = new Subject<void>();

  type: SpinnerType = 'ring-rounded';
  types: SpinnerType[] = ['ring-rounded', 'ring', 'ripple', 'stripes'];
  isActive: boolean;
  @ViewChild('componentSpinner') componentSpinner: ElementRef<HTMLElement>;

  constructor(private spinner: SpinnerService) {
  }

  ngOnInit(): void {
    this.spinner.isActive.pipe(takeUntil(this._destroyStream)).subscribe((isActive) => {
      if (isActive === undefined) {
        return;
      }
      this.isActive = isActive;
    });
  }

  ngOnDestroy(): void {
    this._destroyStream.next();
    this._destroyStream.complete();
  }

  changeType(): void {
    this.type = this.getNewType();
  }

  private getNewType(): SpinnerType {
    const withoutCurrent = this.types.filter((t) => t !== this.type);
    const typesCount = withoutCurrent.length;
    return withoutCurrent[Math.floor(Math.random() * typesCount)];
  }

}

type SpinnerType = 'ring-rounded' | 'ring' | 'ripple' | 'stripes';
