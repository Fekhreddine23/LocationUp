import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { BookingsService, CreateBookingRequest, Booking } from '../../core/services/bookings';
import { AuthService } from '../../core/services/auth.service';
import { OffersService } from '../../core/services/offers.service';
import { User } from '../../core/models/auth.models';
import { Offer } from '../../core/models/offer.model';
import { Breadcrumbs } from "../../components/breadcrumbs/breadcrumbs"; 
import { PaymentService } from '../../core/services/payment.service';
import { IdentityService } from '../../core/services/identity.service';
import { IdentityStatus } from '../../core/models/identity.model';
import { NotificationService } from '../../core/services/notification.service';
import { DriverProfile, createEmptyDriverProfile } from '../../core/models/driver-profile.model';
import { DriverProfileService } from '../../core/services/driver-profile.service';

type BookingStep = 'offer' | 'details' | 'payment';

@Component({
  selector: 'app-create-booking',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, Breadcrumbs],
  templateUrl: './create-booking.component.html',
  styleUrl: './create-booking.component.scss'
})
export class CreateBookingComponent implements OnInit {
  bookingRequest: CreateBookingRequest = {
    userId: 0,
    offerId: 0,
    reservationDate: '',
    driverProfile: undefined
  };

  currentUser: User | null = null;
  preselectedOffer: Offer | null = null;

  selectedOffer: Offer | null = null; // ← Offre sélectionnée

  isLoadingOffer = false;
  errorMessage = '';
  successMessage = '';
  creationLoading = false;
  paymentLoading = false;
  paymentError = '';
  paymentStatusMessage = '';
  createdBooking: Booking | null = null;
  private paymentTriggered = false;
  currentStep: BookingStep = 'offer';
  readonly steps = [
    { id: 'offer' as BookingStep, label: 'Offre', icon: '🎯' },
    { id: 'details' as BookingStep, label: 'Détails', icon: '📝' },
    { id: 'payment' as BookingStep, label: 'Paiement', icon: '💳' }
  ];
  identityStatus: IdentityStatus | null = null;
  identityLoading = false;
  driverProfile: DriverProfile = createEmptyDriverProfile();
  driverProfileLoading = false;
  driverProfileError = '';

  constructor(
    private bookingsService: BookingsService,
    private authService: AuthService,
    private offersService: OffersService,
    private router: Router,
    private route: ActivatedRoute,
    private paymentService: PaymentService,
    private identityService: IdentityService,
    private notificationService: NotificationService,
    private driverProfileService: DriverProfileService
  ) { }

  ngOnInit(): void {
    console.log('=== 🚀 CREATE BOOKING INIT ===');
    
    this.currentUser = this.authService.currentUserValue;
    this.bookingRequest.userId = this.getCurrentUserId();
    
     // Charger uniquement l'offre spécifique depuis l'URL
    this.loadSelectedOffer();
    this.fetchIdentityStatus();
    this.loadDriverProfile();
    
  }

  private loadSelectedOffer(): void {
    this.route.queryParams.subscribe(params => {
      if (params['offerId']) {
        const offerId = +params['offerId'];
        if (offerId > 0) {
          this.bookingRequest.offerId = offerId;
          console.log('🎯 Offer ID from URL:', offerId);
          this.loadOfferDetails(offerId);
          this.currentStep = 'offer';
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

  
  private isFormValid(): boolean {
    return this.bookingRequest.userId > 0 &&
           this.bookingRequest.offerId > 0 &&
           this.bookingRequest.reservationDate !== '' &&
           this.selectedOffer !== null;
  }

  goToDetailsStep(): void {
    if (!this.selectedOffer) {
      this.errorMessage = 'Aucune offre sélectionnée';
      return;
    }
    if (this.identityStatus?.status?.toUpperCase() !== 'VERIFIED') {
      this.errorMessage = 'Veuillez vérifier votre identité avant de poursuivre.';
      this.notificationService.warning('Vérifiez votre identité via votre profil.');
      return;
    }
    this.errorMessage = '';
    this.currentStep = 'details';
  }

  goBackToOffer(): void {
    this.currentStep = 'offer';
  }

  submitDetails(): void {
    if (!this.isFormValid()) {
      this.errorMessage = 'Veuillez remplir tous les champs obligatoires';
      return;
    }

    this.bookingRequest.driverProfile = { ...this.driverProfile };
    this.creationLoading = true;
    this.errorMessage = '';
    this.paymentError = '';
    this.bookingsService.createBooking(this.bookingRequest).subscribe({
      next: (booking) => {
        this.creationLoading = false;
        this.createdBooking = booking;
        this.successMessage = `Réservation #${booking.reservationId} créée !`;
        this.currentStep = 'payment';
        this.paymentStatusMessage = 'Initialisation du paiement...';
        this.paymentTriggered = false;
        this.launchPaymentFromWizard();
      },
      error: (error: any) => {
        this.creationLoading = false;
        this.errorMessage = error.error?.message || 'Erreur lors de la création de la réservation';
        console.error('❌ Error creating booking:', error);
      }
    });
  }

  private launchPaymentFromWizard(): void {
    if (this.paymentTriggered || !this.createdBooking?.reservationId) {
      return;
    }
    this.paymentTriggered = true;
    this.startPayment(this.createdBooking.reservationId);
  }

  onCancel(): void {
    this.router.navigate(['/offers']);
  }

  navigateToBookings(): void {
    this.router.navigate(['/bookings']);
  }

  async startPayment(reservationId: number): Promise<void> {
    try {
      this.paymentError = '';
      this.paymentStatusMessage = 'Redirection vers Stripe...';
      this.paymentLoading = true;
      await this.paymentService.startCheckout(reservationId);
    } catch (error: any) {
      console.error('❌ Payment error:', error);
      this.errorMessage = error?.message || 'Impossible de rediriger vers le paiement. Réessayez depuis vos réservations.';
      this.successMessage = '';
      this.paymentError = this.errorMessage;
      this.paymentStatusMessage = 'Nous n\'avons pas pu initier le paiement.';
      this.paymentLoading = false;
    }
  }

  getStepIndex(step: BookingStep): number {
    return this.steps.findIndex(s => s.id === step);
  }

  isStepCompleted(step: BookingStep): boolean {
    return this.getStepIndex(step) < this.getStepIndex(this.currentStep);
  }

  isStepActive(step: BookingStep): boolean {
    return this.currentStep === step;
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

  breadcrumbItems = [ //breadcrumbs statiques navigation
  { label: 'Mes Réservations', url: '/bookings' },
  { label: 'Nouvelle Réservation', url: '/bookings/new', active: true }
];

  private fetchIdentityStatus(): void {
    this.identityLoading = true;
    this.identityService.getStatus().subscribe({
      next: status => {
        this.identityStatus = status;
        this.identityLoading = false;
      },
      error: err => {
        console.warn('Impossible de charger le statut identité', err);
        this.identityLoading = false;
        this.identityStatus = null;
      }
    });
  }

  private loadDriverProfile(): void {
    this.driverProfileLoading = true;
    this.driverProfileService.getProfile().subscribe({
      next: profile => {
        this.driverProfile = profile ?? createEmptyDriverProfile();
        this.driverProfileLoading = false;
        this.driverProfileError = '';
      },
      error: err => {
        console.warn('Impossible de charger le profil conducteur', err);
        this.driverProfile = createEmptyDriverProfile();
        this.driverProfileLoading = false;
        this.driverProfileError = 'Profil conducteur indisponible. Merci de le compléter dans Mon profil.';
      }
    });
  }
}
