import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OffersService } from '../../core/services/offers.service';
import { OffersMapComponent } from '../../components/offers-map/offers-map.component';
import { Offer } from '../../core/models/offer.model';
import { GeocodingCacheService } from '../../core/services/geocoding-cache.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, FormsModule, OffersMapComponent],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent {
  loading = false;
  error = '';
  offers: Offer[] = [];
  filteredOffers: Offer[] = [];
  categories: string[] = [];
  selectedCategory = 'ALL';
  maxPrice: number | null = null;
  offerDistances = new Map<number, number>();

  userPosition: { lat: number; lng: number } | null = null;
  radiusKm: number | null = null;
  locatingUser = false;

  constructor(
    private offersService: OffersService,
    private geocodingCache: GeocodingCacheService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadOffers();
  }

  loadOffers(): void {
    this.loading = true;
    this.error = '';

    this.offersService.getAllOffers().subscribe({
      next: (offers) => {
        this.offers = offers;
        this.categories = this.extractCategories(offers);
        this.updateDistances();
        this.applyFilters();
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Erreur lors du chargement des offres';
        this.loading = false;
        console.error('Error loading offers:', error);
      }
    });
  }

  onCategoryChange(value: string): void {
    this.selectedCategory = value;
    this.applyFilters();
  }

  onMaxPriceChange(value: string | number | null): void {
    const numeric = typeof value === 'string' ? Number(value) : value;
    this.maxPrice = Number.isFinite(numeric as number) ? Number(numeric) : null;
    this.applyFilters();
  }

  onRadiusChange(value: string | number | null): void {
    const numeric = typeof value === 'string' ? Number(value) : value;
    this.radiusKm = Number.isFinite(numeric as number) && Number(numeric) > 0 ? Number(numeric) : null;
    this.applyFilters();
  }

  requestUserLocation(): void {
    if (this.locatingUser) {
      return;
    }
    if (!navigator.geolocation) {
      this.error = 'La géolocalisation n’est pas supportée sur ce navigateur.';
      return;
    }
    this.locatingUser = true;
    navigator.geolocation.getCurrentPosition(
      (position) => {
        this.userPosition = {
          lat: position.coords.latitude,
          lng: position.coords.longitude
        };
        this.locatingUser = false;
        this.applyFilters();
      },
      () => {
        this.error = 'Impossible de récupérer votre position.';
        this.locatingUser = false;
      },
      {
        enableHighAccuracy: true,
        timeout: 10000
      }
    );
  }

  private applyFilters(): void {
    let result = [...this.offers];
    if (this.userPosition) {
      this.updateDistances();
    }

    if (this.selectedCategory !== 'ALL') {
      result = result.filter((offer) => this.getOfferCategoryLabel(offer).toLowerCase() === this.selectedCategory.toLowerCase());
    }

    if (this.maxPrice !== null && Number.isFinite(this.maxPrice)) {
      result = result.filter((offer) => {
        const priceValue = Number(offer.price);
        return Number.isFinite(priceValue) && priceValue <= (this.maxPrice as number);
      });
    }

    if (this.userPosition && this.radiusKm !== null) {
      result = result.filter((offer) => this.isWithinRadius(offer, this.userPosition!, this.radiusKm!));
    }

    this.filteredOffers = result;
  }

  private extractCategories(offers: Offer[]): string[] {
    const unique = new Set<string>();
    offers.forEach((offer) => unique.add(this.getOfferCategoryLabel(offer)));
    return Array.from(unique).sort((a, b) => a.localeCompare(b));
  }

  getDistanceLabel(offer: Offer): string | null {
    const value = this.offerDistances.get(offer.offerId);
    if (value === undefined) {
      return null;
    }
    return `${value.toFixed(1)} km`;
  }

  private isWithinRadius(offer: Offer, center: { lat: number; lng: number }, radiusKm: number): boolean {
    const coords = this.getOfferCoordinates(offer);
    if (!coords) {
      return false;
    }
    const distance = this.haversineDistance(center.lat, center.lng, coords.lat, coords.lng);
    return distance <= radiusKm;
  }

  private getOfferCoordinates(offer: Offer): { lat: number; lng: number } | null {
    const lat = Number(offer.pickupLatitude);
    const lng = Number(offer.pickupLongitude);
    if (Number.isFinite(lat) && Number.isFinite(lng)) {
      return { lat, lng };
    }

    const cityKey = this.normalizeText(offer.pickupLocationName || offer.pickupLocation || '');
    if (!cityKey) {
      return null;
    }
    const cached = this.geocodingCache.getCachedCoordinate(cityKey);
    if (cached) {
      return { lat: cached.lat, lng: cached.lng };
    }
    return null;
  }

  private haversineDistance(lat1: number, lon1: number, lat2: number, lon2: number): number {
    const R = 6371;
    const toRad = (value: number) => (value * Math.PI) / 180;
    const dLat = toRad(lat2 - lat1);
    const dLon = toRad(lon2 - lon1);
    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
      Math.sin(dLon / 2) * Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
  }

  private updateDistances(): void {
    this.offerDistances.clear();
    if (!this.userPosition) {
      return;
    }
    this.offers.forEach((offer) => {
      const coords = this.getOfferCoordinates(offer);
      if (!coords) {
        return;
      }
      const distance = this.haversineDistance(this.userPosition!.lat, this.userPosition!.lng, coords.lat, coords.lng);
      this.offerDistances.set(offer.offerId, distance);
    });
  }

  getOfferCategoryLabel(offer: any): string {
    const description = this.normalizeText(offer.description || '').toLowerCase();
    const mobilityService = this.normalizeText(this.getMobilityServiceName(offer)).toLowerCase();
    const source = `${description} ${mobilityService}`;

    if (source.includes('velo') || source.includes('bike')) {
      return 'Vélo';
    }
    if (source.includes('trottinette') || source.includes('trotinette')) {
      return 'Trottinette';
    }
    if (source.includes('scooter')) {
      return 'Scooter';
    }
    if (source.includes('moto')) {
      return 'Moto';
    }
    if (source.includes('cargo')) {
      return 'Cargo';
    }
    if (source.includes('voiture') || source.includes('car')) {
      return 'Voiture';
    }
    return 'Mobilité';
  }

  getMobilityServiceDisplay(offer: any): string {
    const name = this.getMobilityServiceName(offer);
    const category = this.getMobilityServiceCategory(offer);

    if (name && category) {
      return `${name} (${category})`;
    }
    if (name) {
      return name;
    }
    if (category) {
      return `Service (${category})`;
    }
    const id = offer?.mobilityServiceId;
    return id ? `Service #${id}` : 'Service indisponible';
  }

  getPickupLocationDisplay(offer: any): string {
    const locationName = this.getPickupLocationName(offer);
    if (locationName) {
      return locationName;
    }
    const id = offer?.pickupLocationId;
    return id ? `Lieu #${id}` : 'Lieu indisponible';
  }

  getOfferImage(offer: any): string {
    return this.offersService.resolveOfferImage(offer);
  }

  private normalizeText(value: string): string {
    return value.normalize('NFD').replace(/[\u0300-\u036f]/g, '');
  }

  private getMobilityServiceName(offer: any): string {
    if (!offer) {
      return '';
    }
    const service = offer.mobilityService;
    if (typeof service === 'string') {
      return service;
    }
    if (service && typeof service === 'object') {
      return service.name || '';
    }
    return offer.mobilityServiceName || '';
  }

  private getMobilityServiceCategory(offer: any): string {
    if (!offer) {
      return '';
    }
    const service = offer.mobilityService;
    if (service && typeof service === 'object') {
      return service.categorie || service.category || '';
    }
    return offer.mobilityServiceCategory || offer.mobilityServiceCategorie || '';
  }

  private getPickupLocationName(offer: any): string {
    if (!offer) {
      return '';
    }
    const location = offer.pickupLocation;
    if (typeof location === 'string') {
      return location;
    }
    if (location && typeof location === 'object') {
      return location.name || '';
    }
    return offer.pickupLocationName || '';
  }

  navigateToBooking(offer: Offer): void {
    if (!offer?.offerId) {
      return;
    }
    this.router.navigate(['/bookings/new'], {
      queryParams: { offerId: offer.offerId }
    });
  }
}
