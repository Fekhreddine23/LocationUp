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
  

  selectedOffer: Offer | null = null; // ← Offre sélectionnée

  isLoading = false;
  isLoadingOffer = false;
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
    
     // Charger uniquement l'offre spécifique depuis l'URL
    this.loadSelectedOffer();
    
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

  private loadSelectedOffer(): void {
    this.route.queryParams.subscribe(params => {
      if (params['offerId']) {
        const offerId = +params['offerId'];
        if (offerId > 0) {
          this.bookingRequest.offerId = offerId;
          console.log('🎯 Offer ID from URL:', offerId);
          this.loadOfferDetails(offerId);
        } else {
          this.errorMessage = 'ID d\'offre invalide';
        }
      } else {
        this.errorMessage = 'Aucune offre sélectionnée';
      }
    });
  }


private loadOfferDetails(offerId: number): void {
    this.isLoadingOffer = true;
    this.errorMessage = '';

    this.offersService.getOfferById(offerId).subscribe({
      next: (offer) => {
        this.selectedOffer = offer;
        this.isLoadingOffer = false;
        console.log('✅ Offer loaded:', offer);
      },
      error: (error) => {
        this.isLoadingOffer = false;
        this.errorMessage = 'Erreur lors du chargement de l\'offre sélectionnée';
        console.error('❌ Error loading offer:', error);
      }
    });
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

  console.log('=== 🧪 DEBUG BOOKING CREATION ===');
  console.log('🔐 Current user:', this.authService.currentUserValue);
  console.log('🔐 User ID in request:', this.bookingRequest.userId);
  console.log('🔐 Token exists:', !!this.authService.getToken());
  console.log('📦 Booking request:', this.bookingRequest);
  console.log('=== 🧪 DEBUG END ===');

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
           this.selectedOffer !== null;
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