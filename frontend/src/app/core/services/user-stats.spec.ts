import { TestBed } from '@angular/core/testing';

import { UserStats } from './user-stats';

describe('UserStats', () => {
  let service: UserStats;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(UserStats);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
