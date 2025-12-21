import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DriverProfile } from '../models/driver-profile.model';

@Injectable({
  providedIn: 'root'
})
export class DriverProfileService {
  private readonly baseUrl = `${environment.apiUrl}/api/profile/driver`;

  constructor(private http: HttpClient) {}

  getProfile(): Observable<DriverProfile> {
    return this.http.get<DriverProfile>(this.baseUrl);
  }

  saveProfile(profile: DriverProfile): Observable<DriverProfile> {
    return this.http.put<DriverProfile>(this.baseUrl, profile);
  }
}
