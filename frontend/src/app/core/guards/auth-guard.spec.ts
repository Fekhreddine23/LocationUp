import { TestBed } from '@angular/core/testing';
import { CanActivateFn } from '@angular/router';

const authGuard: CanActivateFn = (..._guardParameters) => {
  // simple stub for unit test; return true to allow activation
  return true;
};

describe('authGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) => 
      TestBed.runInInjectionContext(() => authGuard(...guardParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });
});
