import { Component, Input } from '@angular/core';
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

 
 
 

 

}
