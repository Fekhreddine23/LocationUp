import { TestBed } from '@angular/core/testing';

import { BusinessEvents } from './business-events';

describe('BusinessEvents', () => {
  let service: BusinessEvents;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(BusinessEvents);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
