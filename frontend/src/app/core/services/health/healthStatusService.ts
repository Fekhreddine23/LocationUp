import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, map, Observable, of, timeout } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { HealthStatusModel } from '../../models/health/Health.model';

@Injectable({
  providedIn: 'root'
})
export class HealthStatusService {
  private readonly baseUrl = environment.apiUrl ?? 'http://localhost:8088';

  constructor(private readonly http: HttpClient) {}

  checkBackendHealth(): Observable<HealthStatusModel> {
    return this.http.get<HealthStatusModel>(`${this.baseUrl}/api/health`).pipe(
      timeout(5000), // Timeout après 5 secondes
      catchError(error => {
        console.error('❌ Health check failed:', error);
        const fallback: HealthStatusModel = {
          status: 'DOWN',
          timestamp: new Date().toISOString(),
          service: 'LocationUp Backend',
          database: 'UNKNOWN',
          liveness: 'DOWN',
          readiness: 'DOWN',
          error: error?.message ?? 'UNKNOWN'
        };
        return of(fallback);
      })
    );
  }
  
  checkLiveness(): Observable<boolean> {
    return this.http.get<void>(`${this.baseUrl}/api/health/liveness`).pipe(
      timeout(3000),
      map(() => true),
      catchError(() => of(false))
    );
  }

  checkReadiness(): Observable<boolean> {
    return this.http.get<void>(`${this.baseUrl}/api/health/readiness`).pipe(
      timeout(3000),
      map(() => true),
      catchError(() => of(false))
    );
  }

  // Méthode pour l'admin dashboard
  getSystemStatus(): Observable<any> {
    return this.checkBackendHealth().pipe(
      map(health => ({
        backend: health.status === 'UP',
        database: health.database === 'UP',
        timestamp: health.timestamp,
        memory: health.memory
      }))
    );
  }
}
