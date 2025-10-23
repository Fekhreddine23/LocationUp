import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { AuthResponse,LoginRequest, RegisterRequest } from '../models/auth.models';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  const mockAuthResponse: AuthResponse = {
    token: 'fake-jwt-token',
    username: 'testuser',
    role: 'user'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });
    
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    
    // Clear localStorage before each test
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify(); // Vérifie qu'aucune requête non traitée
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('login', () => {
    it('should send login request and store user data', () => {
      const loginRequest: LoginRequest = {
        username: 'testuser',
        password: 'password123'
      };

      service.login(loginRequest).subscribe(response => {
        expect(response).toEqual(mockAuthResponse);
        expect(localStorage.getItem('token')).toBe('fake-jwt-token');
        expect(localStorage.getItem('username')).toBe('testuser');
        expect(localStorage.getItem('role')).toBe('user');
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
      expect(req.request.method).toBe('POST');
      req.flush(mockAuthResponse);
    });
  });

  describe('register', () => {
    it('should send register request and store user data', () => {
      const registerRequest: RegisterRequest = {
        username: 'newuser',
        password: 'password123',
        email: 'test@example.com'
      };

      service.register(registerRequest).subscribe(response => {
        expect(response).toEqual(mockAuthResponse);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/auth/register`);
      expect(req.request.method).toBe('POST');
      req.flush(mockAuthResponse);
    });
  });

  describe('logout', () => {
    it('should clear user data from localStorage and BehaviorSubject', () => {
      // Simuler un utilisateur connecté
      localStorage.setItem('token', 'fake-token');
      localStorage.setItem('username', 'testuser');
      localStorage.setItem('role', 'user');
      
      service.logout();

      expect(localStorage.getItem('token')).toBeNull();
      expect(localStorage.getItem('username')).toBeNull();
      expect(localStorage.getItem('role')).toBeNull();
      
      service.currentUser$.subscribe(user => {
        expect(user).toBeNull();
      });
    });
  });

  describe('isLoggedIn', () => {
    it('should return true when token exists', () => {
      localStorage.setItem('token', 'fake-token');
      expect(service.isLoggedIn()).toBeTrue();
    });

    it('should return false when no token exists', () => {
      expect(service.isLoggedIn()).toBeFalse();
    });
  });

  describe('getCurrentUser', () => {
    it('should return current user data when logged in', () => {
      localStorage.setItem('token', 'fake-token');
      localStorage.setItem('username', 'testuser');
      localStorage.setItem('role', 'admin');

      const user = service.getCurrentUser();
      expect(user).toEqual({
        username: 'testuser',
        role: 'admin',
        token: 'fake-token'
      });
    });

    it('should return null when not logged in', () => {
      const user = service.getCurrentUser();
      expect(user).toBeNull();
    });
  });

  describe('initialization', () => {
    it('should initialize with user data from localStorage', () => {
      localStorage.setItem('token', 'existing-token');
      localStorage.setItem('username', 'existing-user');
      localStorage.setItem('role', 'existing-role');

      // Recréer le service pour simuler le redémarrage de l'app
      const newService = TestBed.inject(AuthService);

      newService.currentUser$.subscribe(user => {
        expect(user).toEqual({
          username: 'existing-user',
          role: 'existing-role',
          token: 'existing-token'
        });
      });
    });
  });
});