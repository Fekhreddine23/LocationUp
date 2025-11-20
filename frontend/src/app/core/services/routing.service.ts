import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { Offer } from '../models/offer.model';

export interface RouteResult {
  distanceKm: number;
  durationMinutes: number;
  polyline: [number, number][];
}

@Injectable({
  providedIn: 'root'
})
export class RoutingService {
  private readonly baseUrl = 'https://router.project-osrm.org/route/v1/driving/';

  constructor(private http: HttpClient) {}

  getRoute(offer: Offer): Observable<RouteResult | null> {
    const pickupLat = offer.pickupLatitude;
    const pickupLng = offer.pickupLongitude;
    const returnLat = offer.returnLatitude ?? offer.pickupLatitude;
    const returnLng = offer.returnLongitude ?? offer.pickupLongitude;

    if (
      typeof pickupLat !== 'number' ||
      typeof pickupLng !== 'number' ||
      typeof returnLat !== 'number' ||
      typeof returnLng !== 'number'
    ) {
      return of(null);
    }

    const coordinates = `${pickupLng},${pickupLat};${returnLng},${returnLat}`;
    const params = new HttpParams().set('geometries', 'geojson').set('overview', 'full');

    const headers = new HttpHeaders().set('X-Skip-Auth', 'true');

    return this.http.get<any>(`${this.baseUrl}${coordinates}`, { params, headers }).pipe(
      map((response) => {
        const route = response?.routes?.[0];
        if (!route) {
          return null;
        }
        const distanceKm = (route.distance ?? 0) / 1000;
        const durationMinutes = (route.duration ?? 0) / 60;
        const polyline: [number, number][] = route.geometry?.coordinates ?? [];
        return { distanceKm, durationMinutes, polyline };
      }),
      catchError(() => of(null))
    );
  }
}
