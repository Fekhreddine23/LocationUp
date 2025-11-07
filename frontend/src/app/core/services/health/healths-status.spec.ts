import { TestBed } from '@angular/core/testing';

import { HealthStatus } from './healthStatusService';

describe('HealthsStatus', () => {
  let service: HealthStatus;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(HealthStatus);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
