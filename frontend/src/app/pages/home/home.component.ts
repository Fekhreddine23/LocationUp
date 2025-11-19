import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OffersService } from '../../core/services/offers.service';
import { OffersMapComponent } from '../../components/offers-map/offers-map.component';


@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, OffersMapComponent],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent {
  loading = false;
  error = '';
  offers: any[] = [];
  private readonly defaultOfferImage = 'https://images.unsplash.com/photo-1502877338535-766e1452684a?auto=format&fit=crop&w=900&q=80';

  constructor(private offersService: OffersService) { }

  ngOnInit(): void {
    this.loadOffers();
  }

  loadOffers(): void {
    this.loading = true;
    this.error = '';

    this.offersService.getAllOffers().subscribe({
      next: (offers) => {
        this.offers = offers;
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Erreur lors du chargement des offres';
        this.loading = false;
        console.error('Error loading offers:', error);
      }
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
    return offer?.imageUrl || this.defaultOfferImage;
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
}
