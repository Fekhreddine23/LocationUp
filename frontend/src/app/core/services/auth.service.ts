import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { LoginRequest, RegisterRequest, AuthResponse, User } from '../models/auth.models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8088/api/auth';
  private currentUserSubject: BehaviorSubject<User | null>;
  public currentUser: Observable<User | null>;

  constructor(private http: HttpClient) {
    this.currentUserSubject = new BehaviorSubject<User | null>(
      JSON.parse(localStorage.getItem('currentUser') || 'null')
    );
    this.currentUser = this.currentUserSubject.asObservable();
  }

  login(loginRequest: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, loginRequest)
      .pipe(
        tap(response => {
          console.log('🔐 [AuthService] Login response:', response);
          
          // 🔥 CORRECTION : Utiliser response.userId au lieu de extractUserIdFromToken
          const user: User = {
            id: response.userId,  // ← DIRECT depuis la réponse API
            username: response.username,
            role: response.role,
            token: response.token,
            email: '' // Ajouter l'email si nécessaire
          };
          
          console.log('✅ [AuthService] User created:', user);
          localStorage.setItem('currentUser', JSON.stringify(user));
          localStorage.setItem('token', response.token);
          this.currentUserSubject.next(user);
        })
      );
  }

  register(registerRequest: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, registerRequest)
      .pipe(
        tap(response => {
          console.log('👤 [AuthService] Register response:', response);
          
          // 🔥 CORRECTION : Utiliser response.userId au lieu de extractUserIdFromToken
          const user: User = {
            id: response.userId,  // ← DIRECT depuis la réponse API
            username: response.username,
            role: response.role,
            token: response.token,
            email:'',
          };
          
          console.log('✅ [AuthService] User created:', user);
          localStorage.setItem('currentUser', JSON.stringify(user));
          localStorage.setItem('token', response.token);
          this.currentUserSubject.next(user);
        })
      );
  }

  logout(): void {
    localStorage.removeItem('currentUser');
    localStorage.removeItem('token');
    this.currentUserSubject.next(null);
  }

  isLoggedIn(): boolean {
    return !!this.currentUserValue?.token;
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }



  updateCurrentUser(updatedUser: User): void {
  // Mettre à jour l'utilisateur courant
  this.currentUserSubject.next(updatedUser);
  
  // Mettre à jour le localStorage si présent
  if (typeof localStorage !== 'undefined') {
    localStorage.setItem('currentUser', JSON.stringify(updatedUser));
  }
  
  console.log('✅ Utilisateur mis à jour:', updatedUser);
} 
}