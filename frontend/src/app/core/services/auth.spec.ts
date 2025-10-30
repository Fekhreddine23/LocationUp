import { HttpClient } from '@angular/common/http';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { AuthResponse, LoginRequest, RegisterRequest, User } from '../models/auth.models';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  const apiUrl = 'http://localhost:8088/api/auth';
  const mockAuthResponse: AuthResponse = {
    token: 'fake-jwt-token',
    username: 'testuser',
    role: 'user',
    userId: 0
  };

  const expectedUser: User = {
    id: mockAuthResponse.userId,
    username: mockAuthResponse.username,
    role: mockAuthResponse.role,
    token: mockAuthResponse.token
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });
    
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('login', () => {
    it('should send login request and persist user', (done) => {
      const loginRequest: LoginRequest = {
        username: 'testuser',
        password: 'password123'
      };

      service.login(loginRequest).subscribe(response => {
        expect(response).toEqual(mockAuthResponse);
        expect(localStorage.getItem('token')).toBe(mockAuthResponse.token);
        expect(JSON.parse(localStorage.getItem('currentUser') || 'null')).toEqual(expectedUser);
        expect(service.currentUserValue).toEqual(expectedUser);
        done();
      });

      const req = httpMock.expectOne(`${apiUrl}/login`);
      expect(req.request.method).toBe('POST');
      req.flush(mockAuthResponse);
    });
  });

  describe('register', () => {
    it('should send register request and persist user', (done) => {
      const registerRequest: RegisterRequest = {
        username: 'newuser',
        password: 'password123',
        email: 'test@example.com'
      };

      service.register(registerRequest).subscribe(response => {
        expect(response).toEqual(mockAuthResponse);
        expect(localStorage.getItem('token')).toBe(mockAuthResponse.token);
        expect(JSON.parse(localStorage.getItem('currentUser') || 'null')).toEqual(expectedUser);
        expect(service.currentUserValue).toEqual(expectedUser);
        done();
      });

      const req = httpMock.expectOne(`${apiUrl}/register`);
      expect(req.request.method).toBe('POST');
      req.flush(mockAuthResponse);
    });
  });

  describe('logout', () => {
    it('should clear local storage and emit null user', () => {
      localStorage.setItem('token', expectedUser.token!);
      localStorage.setItem('currentUser', JSON.stringify(expectedUser));
      (service as any).currentUserSubject.next(expectedUser);

      const emissions: Array<User | null> = [];
      const subscription = service.currentUser.subscribe(user => emissions.push(user));

      expect(emissions).toEqual([expectedUser]);
      service.logout();

      expect(localStorage.getItem('token')).toBeNull();
      expect(localStorage.getItem('currentUser')).toBeNull();
      expect(emissions).toEqual([expectedUser, null]);

      subscription.unsubscribe();
    });
  });

  describe('isLoggedIn', () => {
    it('should return true when current user has a token', () => {
      (service as any).currentUserSubject.next(expectedUser);
      expect(service.isLoggedIn()).toBeTrue();
    });

    it('should return false when current user is null', () => {
      (service as any).currentUserSubject.next(null);
      expect(service.isLoggedIn()).toBeFalse();
    });
  });

  describe('currentUserValue', () => {
    it('should return current user data when logged in', () => {
      (service as any).currentUserSubject.next(expectedUser);
      expect(service.currentUserValue).toEqual(expectedUser);
    });

    it('should return null when not logged in', () => {
      (service as any).currentUserSubject.next(null);
      expect(service.currentUserValue).toBeNull();
    });
  });

  describe('initialization', () => {
    it('should initialize with user data from localStorage', () => {
      const storedUser: User = {
        id: 42,
        username: 'existing-user',
        role: 'existing-role',
        token: 'existing-token'
      };

      localStorage.setItem('currentUser', JSON.stringify(storedUser));
      localStorage.setItem('token', storedUser.token!);

      const http = TestBed.inject(HttpClient);
      const newService = new AuthService(http);

      expect(newService.currentUserValue).toEqual(storedUser);

      let observed: User | null | undefined;
      const subscription = newService.currentUser.subscribe(user => observed = user);
      expect(observed).toEqual(storedUser);
      subscription.unsubscribe();
    });
  });
});
