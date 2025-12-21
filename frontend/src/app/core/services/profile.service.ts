import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface AvatarResponse {
  filename: string;
  url: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProfileService {
  private readonly baseUrl = `${environment.apiUrl}/api/users`;

  constructor(private http: HttpClient) {}

  getAvatar(): Observable<AvatarResponse> {
    return this.http.get<AvatarResponse>(`${this.baseUrl}/me/avatar`);
  }

  uploadAvatar(file: File): Observable<AvatarResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<AvatarResponse>(`${this.baseUrl}/me/avatar`, formData);
  }

  deleteAvatar(): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/me/avatar`);
  }
}
