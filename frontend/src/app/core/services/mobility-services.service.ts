import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { MobilityService as MobilityServiceModel } from '../models/MobilityService.model';


@Injectable({
  providedIn: 'root'
})
export class MobilityService {
  private apiUrl = `${environment.apiUrl}/api/mobility-services`;

  constructor(private http: HttpClient) {}

  // GET /api/mobility-services - Tous les services
  getAllServices(): Observable<MobilityServiceModel[]> {
    return this.http.get<MobilityServiceModel[]>(this.apiUrl);
  }

  // GET /api/mobility-services/{id} - Service par ID
  getServiceById(id: number): Observable<MobilityServiceModel> {
    return this.http.get<MobilityServiceModel>(`${this.apiUrl}/${id}`);
  }

  // GET /api/mobility-services/name/{name} - Service par nom
  getServiceByName(name: string): Observable<MobilityServiceModel> {
    return this.http.get<MobilityServiceModel>(`${this.apiUrl}/name/${name}`);
  }

  // POST /api/mobility-services - Créer un service
  createService(serviceData: { name: string; description: string }): Observable<MobilityServiceModel> {
    return this.http.post<MobilityServiceModel>(this.apiUrl, serviceData);
  }

  // DELETE /api/mobility-services/{id} - Supprimer un service
  deleteService(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // GET /api/mobility-services/category/{category} - Services par catégorie
  getServicesByCategory(category: string): Observable<MobilityServiceModel[]> {
    return this.http.get<MobilityServiceModel[]>(`${this.apiUrl}/category/${category}`);
  }
}
