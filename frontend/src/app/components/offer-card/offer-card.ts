import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router'; // ← Ajouter Router
import { OffersService, } from '../../core/services/offers.service';
import { Offer, OfferStatus } from '../../core/models/offer.model';

@Component({
  selector: 'app-offer-card',
  standalone: true,
  imports: [CommonModule], // ← Ajouter CommonModule si besoin
  templateUrl: './offer-card.component.html',
  styleUrl: './offer-card.component.scss'
})
export class OfferCardComponent {

  @Input() offer!: Offer;
  @Input() isFavorite = false;
  @Input() quickViewActive = false;
  @Output() favoriteToggled = new EventEmitter<Offer>();
  @Output() detailsRequested = new EventEmitter<Offer>();
  private readonly defaultImage = 'https://images.unsplash.com/photo-1477847616630-cf9cf8815fda?auto=format&fit=crop&w=900&q=80';

  constructor(
    private offersService: OffersService,
    private router: Router // ← Injecter Router
  ) { }

  formatPrice(price: number): string {
    return this.offersService.formatPrice(price);
  }

  formatDate(isoDate: string): string {
    return this.offersService.formatDateForDisplay(isoDate);
  }

  onReserve(): void {
    // Navigation vers le formulaire de réservation avec l'ID de l'offre
    this.router.navigate(['/bookings/new'], {
      queryParams: { offerId: this.offer.offerId }
    });
  }

  
  viewOfferDetails(offerId: number): void {
    this.detailsRequested.emit(this.offer);
    this.router.navigate(['/offers', offerId]);
  }


  //methode categorie mobility service
  getStatusText(status?: OfferStatus): string {
    const map: Record<OfferStatus, string> = {
      PENDING: 'En attente',
      CONFIRMED: 'Confirmée',
      CANCELLED: 'Annulée',
      COMPLETED: 'Terminée'
    };
    return map[this.normalizeStatus(status)];
  }

  getStatusClass(status?: OfferStatus): string {
    const map: Record<OfferStatus, string> = {
      PENDING: 'status-pending',
      CONFIRMED: 'status-confirmed',
      CANCELLED: 'status-cancelled',
      COMPLETED: 'status-completed'
    };
    return map[this.normalizeStatus(status)];
  }

  getOfferImage(offer: Offer): string {
    return offer.imageUrl || this.defaultImage;
  }

  getPickupLabel(offer: Offer): string {
    return (
      offer.pickupLocationName ||
      offer.pickupLocation ||
      offer.pickupLocationCity ||
      `Lieu #${offer.pickupLocationId ?? offer.offerId}`
    );
  }

  getReturnLabel(offer: Offer): string {
    return (
      offer.returnLocationName ||
      offer.returnLocation ||
      offer.returnLocationCity ||
      `Lieu #${offer.returnLocationId ?? offer.offerId}`
    );
  }

  getOfferCategoryLabel(offer: Offer): string {
    const description = this.normalizeText(offer.description || '').toLowerCase();
    const service = this.normalizeText(offer.mobilityService || '').toLowerCase();
    const source = `${description} ${service}`;

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
    if (source.includes('camion') || source.includes('cargo')) {
      return 'Cargo';
    }
    if (source.includes('voiture') || source.includes('car')) {
      return 'Voiture';
    }
    if (source.includes('van') || source.includes('utilitaire')) {
      return 'Van';
    }
    return 'Mobilité';
  }

  private normalizeText(value: string): string {
    return value.normalize('NFD').replace(/[\u0300-\u036f]/g, '');
  }

  isOfferConfirmed(offer: Offer): boolean {
    return this.normalizeStatus(offer.status) === 'CONFIRMED';
  }

  private normalizeStatus(status?: OfferStatus | null): OfferStatus {
    if (!status) {
      return 'PENDING';
    }
    return ['PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED'].includes(status)
      ? status
      : 'PENDING';
  }

  onToggleFavorite(event: MouseEvent): void {
    event.stopPropagation();
    this.favoriteToggled.emit(this.offer);
  }

  onQuickDetails(event: MouseEvent): void {
    event.stopPropagation();
    this.detailsRequested.emit(this.offer);
  }
}
