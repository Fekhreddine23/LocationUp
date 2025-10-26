import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface MobilityService {
  serviceId: number;
  name: string;
  description: string;
}

@Injectable({
  providedIn: 'root'
})
export class MobilityServicesService {
  private apiUrl = `${environment.apiUrl}/api/mobility-services`;

  constructor(private http: HttpClient) {}

  // GET /api/mobility-services - Tous les services
  getAllServices(): Observable<MobilityService[]> {
    return this.http.get<MobilityService[]>(this.apiUrl);
  }

  // GET /api/mobility-services/{id} - Service par ID
  getServiceById(id: number): Observable<MobilityService> {
    return this.http.get<MobilityService>(`${this.apiUrl}/${id}`);
  }

  // GET /api/mobility-services/name/{name} - Service par nom
  getServiceByName(name: string): Observable<MobilityService> {
    return this.http.get<MobilityService>(`${this.apiUrl}/name/${name}`);
  }

  // POST /api/mobility-services - Créer un service
  createService(serviceData: { name: string; description: string }): Observable<MobilityService> {
    return this.http.post<MobilityService>(this.apiUrl, serviceData);
  }

  // DELETE /api/mobility-services/{id} - Supprimer un service
  deleteService(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}