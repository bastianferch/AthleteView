import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TrainingsplanComponent } from './trainingsplan.component';

describe('TrainingsplanComponent', () => {
  let component: TrainingsplanComponent;
  let fixture: ComponentFixture<TrainingsplanComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [TrainingsplanComponent],
    });
    fixture = TestBed.createComponent(TrainingsplanComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
