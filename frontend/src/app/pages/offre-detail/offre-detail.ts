import { Component, OnInit } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { Offer } from '../../core/models/offer.model';
import { OffersService } from '../../core/services/offers.service';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-offre-detail',
  standalone: true,
  imports: [CommonModule, CurrencyPipe],
  templateUrl: './offre-detail.html',
  styleUrl: './offre-detail.scss'
})
export class OffreDetail implements OnInit  {

  offer: Offer | null = null;
  isLoading = false;
  errorMessage = '';
  
  // Pour formater les dates
  currentDate = new Date();

  constructor(
    private offersService: OffersService,
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadOfferDetails();
  }

  private loadOfferDetails(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.route.params.subscribe(params => {
      const offerId = +params['id'];
      
      if (offerId > 0) {
        this.offersService.getOfferById(offerId).subscribe({
          next: (offer) => {
            this.offer = offer;
            this.isLoading = false;
            console.log('✅ Offer details loaded:', offer);
          },
          error: (error) => {
            this.isLoading = false;
            this.errorMessage = 'Erreur lors du chargement des détails de l\'offre';
            console.error('❌ Error loading offer details:', error);
          }
        });
      } else {
        this.errorMessage = 'ID d\'offre invalide';
        this.isLoading = false;
      }
    });
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  isOfferAvailable(): boolean {
    if (!this.offer) return false;
    const pickupDate = new Date(this.offer.pickupDatetime);
    return pickupDate > this.currentDate;
  }

  reserveOffer(): void {
    if (this.offer) {
      this.router.navigate(['/bookings/new'], { 
        queryParams: { offerId: this.offer.offerId } 
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/offers']);
  }

}
