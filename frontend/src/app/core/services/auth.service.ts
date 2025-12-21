import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { LoginRequest, RegisterRequest, AuthResponse, User } from '../models/auth.models';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}/api/auth`;
  private currentUserSubject: BehaviorSubject<User | null>;
  public currentUser: Observable<User | null>;
  private accessTokenSubject = new BehaviorSubject<string | null>(null);

  constructor(private http: HttpClient) {
    this.currentUserSubject = new BehaviorSubject<User | null>(
      JSON.parse(localStorage.getItem('currentUser') || 'null')
    );
    const existingToken = sessionStorage.getItem('access_token');
    if (existingToken) {
      this.accessTokenSubject.next(existingToken);
    }
    this.currentUser = this.currentUserSubject.asObservable();
  }

  login(loginRequest: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, loginRequest, { withCredentials: true })
      .pipe(
        tap(response => {
          // 🔥 CORRECTION : Utiliser response.userId au lieu de extractUserIdFromToken
          const user: User = {
            id: response.userId,  // ← DIRECT depuis la réponse API
            username: response.username,
            role: response.role,
            token: response.token,
            email: '', // Ajouter l'email si nécessaire
            avatarUrl: this.buildAvatarUrl(response.avatarUrl)
          };
          
          localStorage.setItem('currentUser', JSON.stringify(user));
          this.setAccessToken(response.token);
          this.currentUserSubject.next(user);
        })
      );
  }

  register(registerRequest: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, registerRequest, { withCredentials: true })
      .pipe(
        tap(response => {
          // 🔥 CORRECTION : Utiliser response.userId au lieu de extractUserIdFromToken
          const user: User = {
            id: response.userId,  // ← DIRECT depuis la réponse API
            username: response.username,
            role: response.role,
            token: response.token,
            email:'',
            avatarUrl: this.buildAvatarUrl(response.avatarUrl)
          };
          
          localStorage.setItem('currentUser', JSON.stringify(user));
          this.setAccessToken(response.token);
          this.currentUserSubject.next(user);
        })
      );
  }

  logout(): void {
    localStorage.removeItem('currentUser');
    sessionStorage.removeItem('access_token');
    this.currentUserSubject.next(null);
    this.accessTokenSubject.next(null);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  getToken(): string | null {
    return this.accessTokenSubject.value;
  }

  get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  hasRole(role: string): boolean {
    const current = this.currentUserValue;
    if (!current || !role) {
      return false;
    }
    return current.role === role;
  }

  hasAnyRole(roles: string[]): boolean {
    if (!roles || roles.length === 0) {
      return false;
    }
    return roles.some(role => this.hasRole(role));
  }

  refreshAccessToken(): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/refresh`, {}, { withCredentials: true })
      .pipe(
        tap(res => {
          if (res?.token) {
            this.setAccessToken(res.token);
            const current = this.currentUserValue;
            if (current) {
              const updated: User = { ...current, token: res.token };
              localStorage.setItem('currentUser', JSON.stringify(updated));
              this.currentUserSubject.next(updated);
            }
          }
        })
      );
  }


  updateCurrentUser(updatedUser: User): void {
    this.currentUserSubject.next(updatedUser);
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem('currentUser', JSON.stringify(updatedUser));
    }
  }

  private buildAvatarUrl(relativeUrl?: string): string | undefined {
    if (!relativeUrl) {
      return undefined;
    }
    return `${environment.apiUrl}${relativeUrl}`;
  }

  private setAccessToken(token: string | null): void {
    this.accessTokenSubject.next(token);
    if (token) {
      sessionStorage.setItem('access_token', token);
    } else {
      sessionStorage.removeItem('access_token');
    }
  }
}
