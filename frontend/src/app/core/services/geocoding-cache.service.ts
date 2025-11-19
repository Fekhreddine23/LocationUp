import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';

export interface Coordinates {
  lat: number;
  lng: number;
}

@Injectable({
  providedIn: 'root'
})
export class GeocodingCacheService {
  private static readonly STORAGE_KEY = 'locationUpGeoCache';
  private cache: Record<string, Coordinates> = {};

  constructor(private http: HttpClient) {
    this.cache = this.loadCache();
  }

  getCachedCoordinate(cityKey: string): Coordinates | null {
    return this.cache[cityKey] ?? null;
  }

  saveCoordinate(cityKey: string, coords: Coordinates): void {
    this.cache[cityKey] = coords;
    localStorage.setItem(GeocodingCacheService.STORAGE_KEY, JSON.stringify(this.cache));
  }

  geocode(cityName: string): Observable<Coordinates | null> {
    const normalized = this.normalize(cityName);
    if (!normalized) {
      return of(null);
    }

    const cached = this.getCachedCoordinate(normalized);
    if (cached) {
      return of(cached);
    }

    const params = new HttpParams()
      .set('q', cityName)
      .set('format', 'json')
      .set('limit', '1')
      .set('addressdetails', '0');

    return this.http
      .get<any[]>('https://nominatim.openstreetmap.org/search', {
        params,
        headers: {
          'Accept-Language': 'fr'
        }
      })
      .pipe(
        map((response) => {
          if (!Array.isArray(response) || response.length === 0) {
            return null;
          }
          const match = response[0];
          const lat = parseFloat(match.lat);
          const lng = parseFloat(match.lon);
          if (Number.isNaN(lat) || Number.isNaN(lng)) {
            return null;
          }
          return { lat, lng };
        }),
        tap((coords) => {
          if (coords) {
            this.saveCoordinate(normalized, coords);
          }
        }),
        catchError(() => of(null))
      );
  }

  private loadCache(): Record<string, Coordinates> {
    try {
      const raw = localStorage.getItem(GeocodingCacheService.STORAGE_KEY);
      return raw ? (JSON.parse(raw) as Record<string, Coordinates>) : {};
    } catch {
      return {};
    }
  }

  private normalize(value: string): string {
    return value
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .trim()
      .toLowerCase();
  }
}
