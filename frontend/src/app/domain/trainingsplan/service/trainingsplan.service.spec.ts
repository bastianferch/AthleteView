import { TestBed } from '@angular/core/testing';

import { TrainingsplanService } from './trainingsplan.service';

describe('TrainingsplanService', () => {
  let service: TrainingsplanService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TrainingsplanService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
