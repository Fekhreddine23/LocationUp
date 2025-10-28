import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router'; // ← Ajouter Router
import { OffersService, } from '../../core/services/offers.service';
import { Offer } from '../../core/models/offer.model';

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
}