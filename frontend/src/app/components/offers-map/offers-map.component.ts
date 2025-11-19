import { CommonModule } from '@angular/common';
import { Component, Input, OnChanges, OnDestroy, SimpleChanges } from '@angular/core';
import { LeafletModule } from '@bluehalo/ngx-leaflet';
import * as L from 'leaflet';
import { Offer } from '../../core/models/offer.model';
import { GeocodingCacheService } from '../../core/services/geocoding-cache.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-offers-map',
  standalone: true,
  imports: [CommonModule, LeafletModule],
  templateUrl: './offers-map.component.html',
  styleUrl: './offers-map.component.scss'
})
export class OffersMapComponent implements OnChanges, OnDestroy {
  @Input() offers: Offer[] = [];
  private static readonly FALLBACK_COORDS: Record<string, [number, number]> = {
    paris: [48.8566, 2.3522],
    lyon: [45.764, 4.8357],
    marseille: [43.2965, 5.3698],
    bordeaux: [44.8378, -0.5792],
    nice: [43.7102, 7.262],
    toulouse: [43.6047, 1.4442],
    lille: [50.6292, 3.0573],
    strasbourg: [48.5734, 7.7521]
  };

  readonly mapOptions: L.MapOptions = {
    layers: [
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution:
          '&copy; <a href="https://www.openstreetmap.org/copyright" rel="noopener" target="_blank">OpenStreetMap</a> contributors'
      })
    ],
    zoom: 5,
    center: L.latLng(46.603354, 1.888334)
  };

  markerLayers: L.Layer[] = [];
  private mapInstance?: L.Map;
  private cachedCoordinates = new Map<string, L.LatLngExpression>();
  private pendingCityRequests = new Set<string>();
  private subscriptions: Subscription[] = [];

  constructor(private geocodingCache: GeocodingCacheService) {
    this.configureLeafletIcons();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['offers']) {
      this.updateMarkers();
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((sub) => sub.unsubscribe());
  }

  onMapReady(map: L.Map): void {
    this.mapInstance = map;
    this.updateMapView();
  }

  private updateMarkers(): void {
    const markers = (this.offers ?? [])
      .map((offer) => {
        const coords = this.resolveCoordinates(offer);
        if (!coords) {
          return null;
        }
        return L.marker(coords, {
          title: offer.description
        }).bindPopup(this.buildPopupContent(offer));
      })
      .filter((marker): marker is L.Marker => !!marker);

    this.markerLayers = markers;
    this.updateMapView();
  }

  private updateMapView(): void {
    if (!this.mapInstance) {
      return;
    }

    if (this.markerLayers.length === 0) {
      this.mapInstance.setView(L.latLng(46.603354, 1.888334), 5);
      return;
    }

    const markerGroup = L.featureGroup(this.markerLayers.filter((layer): layer is L.Marker => layer instanceof L.Marker));
    const bounds = markerGroup.getBounds();

    if (bounds.isValid()) {
      this.mapInstance.fitBounds(bounds.pad(0.2));
    }
  }

  private resolveCoordinates(offer: Offer): L.LatLngExpression | null {
    if (
      typeof offer.pickupLatitude === 'number' &&
      typeof offer.pickupLongitude === 'number' &&
      !Number.isNaN(offer.pickupLatitude) &&
      !Number.isNaN(offer.pickupLongitude)
    ) {
      return [offer.pickupLatitude, offer.pickupLongitude];
    }

    const cityKey = this.normalizeText(offer.pickupLocationName || offer.pickupLocation || '');
    if (!cityKey) {
      return null;
    }

    if (OffersMapComponent.FALLBACK_COORDS[cityKey]) {
      return OffersMapComponent.FALLBACK_COORDS[cityKey];
    }

    if (this.cachedCoordinates.has(cityKey)) {
      return this.cachedCoordinates.get(cityKey)!;
    }

    const cached = this.geocodingCache.getCachedCoordinate(cityKey);
    if (cached) {
      const coords: L.LatLngExpression = [cached.lat, cached.lng];
      this.cachedCoordinates.set(cityKey, coords);
      return coords;
    }

    if (!this.pendingCityRequests.has(cityKey)) {
      this.pendingCityRequests.add(cityKey);
      const sub = this.geocodingCache.geocode(offer.pickupLocationName || offer.pickupLocation || cityKey).subscribe((coords) => {
        this.pendingCityRequests.delete(cityKey);
        if (coords) {
          const latLng: L.LatLngExpression = [coords.lat, coords.lng];
          this.cachedCoordinates.set(cityKey, latLng);
          this.updateMarkers();
        }
      });
      this.subscriptions.push(sub);
    }

    return null;
  }

  private normalizeText(value: string): string {
    return value
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .trim()
      .toLowerCase();
  }

  private buildPopupContent(offer: Offer): string {
    const service = offer.mobilityService || offer.mobilityServiceId ? `Service : ${offer.mobilityService || `#${offer.mobilityServiceId}`}` : '';
    const location = offer.pickupLocationName || offer.pickupLocation ? `Lieu : ${offer.pickupLocationName || offer.pickupLocation}` : '';
    const price = offer.price ? `Tarif : ${offer.price.toFixed(2)}€` : '';

    return [offer.description, service, location, price].filter(Boolean).join('<br/>');
  }

  private configureLeafletIcons(): void {
    const iconRetinaUrl = new URL('leaflet/dist/images/marker-icon-2x.png', import.meta.url).toString();
    const iconUrl = new URL('leaflet/dist/images/marker-icon.png', import.meta.url).toString();
    const shadowUrl = new URL('leaflet/dist/images/marker-shadow.png', import.meta.url).toString();

    const defaultIcon = L.icon({
      iconRetinaUrl,
      iconUrl,
      shadowUrl,
      iconSize: [25, 41],
      iconAnchor: [12, 41],
      popupAnchor: [1, -34],
      tooltipAnchor: [16, -28],
      shadowSize: [41, 41]
    });

    L.Marker.prototype.options.icon = defaultIcon;
  }
}
