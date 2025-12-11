import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { IdentitySessionResponse, IdentityStatus } from '../models/identity.model';

@Injectable({
  providedIn: 'root'
})
export class IdentityService {
  private readonly baseUrl = `${environment.apiUrl}/api/identity`;

  constructor(private http: HttpClient) {}

  createSession(returnUrl?: string): Observable<IdentitySessionResponse> {
    return this.http.post<IdentitySessionResponse>(`${this.baseUrl}/session`, {
      returnUrl
    });
  }

  getStatus(): Observable<IdentityStatus> {
    return this.http.get<IdentityStatus>(`${this.baseUrl}/status`);
  }
}
