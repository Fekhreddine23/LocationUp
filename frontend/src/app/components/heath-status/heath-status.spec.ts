import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { HeathStatus } from './heath-status';
import { HealthStatusService } from '../../core/services/health/healthStatusService';
import { AuthService } from '../../core/services/auth.service';

class MockHealthStatusService {
  checkLiveness() {
    return of(true);
  }
}

class MockAuthService {
  currentUser = of({ role: 'ROLE_ADMIN' });
}

describe('HeathStatus', () => {
  let component: HeathStatus;
  let fixture: ComponentFixture<HeathStatus>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HeathStatus],
      providers: [
        { provide: HealthStatusService, useClass: MockHealthStatusService },
        { provide: AuthService, useClass: MockAuthService }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HeathStatus);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
