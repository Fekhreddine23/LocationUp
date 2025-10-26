import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { BookingsService, CreateBookingRequest } from '../../core/services/bookings';
import { AuthService } from '../../core/services/auth.service';
import { OffersService } from '../../core/services/offers.service';
import { User } from '../../core/models/auth.models';
import { Offer } from '../../core/models/offer.model'; 

@Component({
  selector: 'app-create-booking',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './create-booking.component.html',
  styleUrl: './create-booking.component.scss'
})
export class CreateBookingComponent implements OnInit {
  bookingRequest: CreateBookingRequest = {
    userId: 0,
    offerId: 0,
    reservationDate: ''
  };

  offers: Offer[] = [];
  currentUser: User | null = null;
  preselectedOffer: Offer | null = null;
  
  isLoading = false;
  isLoadingOffers = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private bookingsService: BookingsService,
    private authService: AuthService,
    private offersService: OffersService,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    console.log('=== 🚀 CREATE BOOKING INIT ===');
    
    this.currentUser = this.authService.currentUserValue;
    this.bookingRequest.userId = this.getCurrentUserId();
    
    // Charger les offres
    this.loadOffers();
    
    // Vérifier l'offerId dans l'URL
    this.route.queryParams.subscribe(params => {
      if (params['offerId']) {
        const offerId = +params['offerId'];
        if (offerId > 0) {
          this.bookingRequest.offerId = offerId;
          console.log('🎯 Offer preselected from URL:', offerId);
        }
      }
    });
  }

  private loadOffers(): void {
    this.isLoadingOffers = true;
    
    this.offersService.getAllOffers().subscribe({
      next: (offers) => {
        this.offers = offers;
        this.isLoadingOffers = false;
        console.log('✅ Offers loaded:', this.offers);
        
        // Si une offre était présélectionnée, la trouver
        if (this.bookingRequest.offerId > 0) {
          this.preselectedOffer = this.offers.find(o => o.offerId === this.bookingRequest.offerId) || null;
          console.log('🎯 Preselected offer found:', this.preselectedOffer);
        }
        
        // Sélectionner la première offre par défaut si aucune sélection
        if (!this.bookingRequest.offerId && this.offers.length > 0) {
          const availableOffer = this.offers.find(o => this.isOfferAvailable(o));
          if (availableOffer) {
            this.bookingRequest.offerId = availableOffer.offerId;
          }
        }
      },
      error: (error) => {
        this.isLoadingOffers = false;
        console.error('❌ Error loading offers:', error);
        this.errorMessage = 'Erreur lors du chargement des offres disponibles';
      }
    });
  }

  // Sélectionner une offre (pour la liste visuelle)
  selectOffer(offer: Offer): void {
    if (this.isOfferAvailable(offer)) {
      this.bookingRequest.offerId = offer.offerId;
      console.log('✅ Offer selected:', offer.offerId);
    }
  }

  // Obtenir l'offre sélectionnée
  getSelectedOffer(): Offer | null {
    return this.offers.find(o => o.offerId === this.bookingRequest.offerId) || null;
  }

  // Vérifier si une offre est disponible
  isOfferAvailable(offer: Offer): boolean {
    const now = new Date();
    const pickupDate = new Date(offer.pickupDatetime);
    return pickupDate > now;
  }

  getMinDate(): string {
    const now = new Date();
    return now.toISOString().slice(0, 16);
  }

  getMaxDate(): string {
    const oneYearFromNow = new Date();
    oneYearFromNow.setFullYear(oneYearFromNow.getFullYear() + 1);
    return oneYearFromNow.toISOString().slice(0, 16);
  }

  onSubmit(): void {
    if (!this.isFormValid()) {
      this.errorMessage = 'Veuillez remplir tous les champs obligatoires';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    console.log('📦 Creating reservation:', this.bookingRequest);

    this.bookingsService.createBooking(this.bookingRequest).subscribe({
      next: (booking) => {
        this.isLoading = false;
        this.successMessage = `Réservation #${booking.reservationId} créée avec succès !`;
        console.log('✅ Reservation created:', booking);
      },
      error: (error: any) => {
        this.isLoading = false;
        this.errorMessage = error.error?.message || 'Erreur lors de la création de la réservation';
        console.error('❌ Error creating booking:', error);
      }
    });
  }

  private isFormValid(): boolean {
    return this.bookingRequest.userId > 0 &&
           this.bookingRequest.offerId > 0 &&
           this.bookingRequest.reservationDate !== '' &&
           this.getSelectedOffer() !== null;
  }

  onCancel(): void {
    this.router.navigate(['/offers']);
  }

  navigateToBookings(): void {
    this.router.navigate(['/bookings']);
  }

  private getCurrentUserId(): number {
    const currentUser = this.authService.currentUserValue;
    if (currentUser?.id) {
      return currentUser.id;
    }
    
    const userData = localStorage.getItem('currentUser');
    if (userData) {
      try {
        const user = JSON.parse(userData);
        if (user.id) {
          return user.id;
        }
      } catch (error) {
        console.error('Error parsing user data:', error);
      }
    }
    
    throw new Error('Utilisateur non authentifié');
  }
}