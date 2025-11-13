import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { TwoFactorSetup } from './two-factor-setup';
import { TwoFactorAuthService } from '../../core/services/twoFactor/two-factor-auth-service';

class TwoFactorAuthServiceMock {
  setup2FA() {
    return of({ secret: 'secret', qrCodeUrl: 'data:image/png;base64,...', message: 'ok' });
  }

  verify2FA() {
    return of({ valid: true, message: 'ok' });
  }

  get2FAStatus() {
    return of({ enabled: false, username: 'john' });
  }

  generateBackupCode() {
    return of({ backupCode: 'ABC123', message: 'ok' });
  }
}

describe('TwoFactorSetup', () => {
  let component: TwoFactorSetup;
  let fixture: ComponentFixture<TwoFactorSetup>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TwoFactorSetup],
      providers: [{ provide: TwoFactorAuthService, useClass: TwoFactorAuthServiceMock }]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TwoFactorSetup);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
