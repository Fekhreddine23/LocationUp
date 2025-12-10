import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Subscription } from 'rxjs';
import { BookingsService, Booking } from '../../core/services/bookings';
import { PaymentService } from '../../core/services/payment.service';

@Component({
  selector: 'app-payment-retry',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
  <div class="payment-feedback retry">
    <h1>🔁 Relancer mon paiement</h1>
    <p>Vous pouvez relancer un paiement en attente ou en échec sur cette page.</p>

    <div *ngIf="isLoading" class="loading-state">
      <div class="spinner"></div>
      <p>Analyse des réservations en cours...</p>
    </div>

    <div *ngIf="infoMessage && !isLoading" class="alert subtle">
      {{ infoMessage }}
    </div>

    <div *ngIf="errorMessage" class="alert">
      {{ errorMessage }}
    </div>

    <ng-container *ngIf="bookingOptions.length > 1 && !isLoading">
      <div class="booking-options-card">
        <p>Plusieurs réservations correspondent à votre demande. Sélectionnez celle à relancer :</p>
        <div class="options-list">
          <button
            type="button"
            class="btn secondary"
            *ngFor="let option of bookingOptions"
            [class.active]="option.reservationId === reservationId"
            (click)="selectBooking(option)">
            #{{ option.reservationId }} &middot;
            {{ option.reservationDate | date:'dd/MM à HH:mm' }} &middot;
            {{ option.paymentStatus || 'PENDING' }}
          </button>
        </div>
      </div>
    </ng-container>

    <ng-container *ngIf="selectedBooking">
      <div class="booking-recap">
        <div class="recap-row">
          <span>Réservation</span>
          <strong>#{{ selectedBooking.reservationId }}</strong>
        </div>
        <div class="recap-row">
          <span>Offre</span>
          <strong>#{{ selectedBooking.offerId }}</strong>
        </div>
        <div class="recap-row">
          <span>Date du trajet</span>
          <strong>{{ selectedBooking.reservationDate | date:'dd/MM/yyyy à HH:mm' }}</strong>
        </div>
        <div class="recap-row">
          <span>Statut paiement</span>
          <strong>{{ selectedBooking.paymentStatus || 'PENDING' }}</strong>
        </div>
      </div>

      <div class="actions">
        <button class="btn secondary" type="button" (click)="gotoBookings()">
          📋 Voir mes réservations
        </button>
        <button class="btn primary" type="button"
                [disabled]="retrying"
                (click)="retryPayment()">
          {{ retrying ? 'Relance en cours...' : 'Relancer ce paiement' }}
        </button>
      </div>
    </ng-container>

    <div *ngIf="!selectedBooking && !isLoading && !errorMessage" class="empty-state">
      <p>Aucune réservation éligible trouvée. Vérifiez vos réservations pour relancer un paiement.</p>
      <button class="btn secondary" type="button" (click)="gotoBookings()">
        📋 Ouvrir mes réservations
      </button>
    </div>
  </div>
  `,
  styleUrls: ['./payment-feedback.scss']
})
export class PaymentRetryComponent implements OnInit, OnDestroy {
  reservationId: number | null = null;
  offerId: number | null = null;
  selectedBooking: Booking | null = null;
  bookingOptions: Booking[] = [];
  isLoading = false;
  retrying = false;
  errorMessage = '';
  infoMessage = '';

  private paramsSub?: Subscription;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private bookingsService: BookingsService,
    private paymentService: PaymentService
  ) {}

  ngOnInit(): void {
    this.paramsSub = this.route.queryParamMap.subscribe(params => {
      this.errorMessage = '';
      this.infoMessage = '';
      this.reservationId = null;
      this.offerId = null;
      this.selectedBooking = null;
      this.bookingOptions = [];

      const reservationParam = params.get('reservationId');
      const offerParam = params.get('offerId');

      if (reservationParam) {
        const parsed = Number(reservationParam);
        if (!isNaN(parsed) && parsed > 0) {
          this.reservationId = parsed;
          this.loadBookingByReservation(parsed);
          return;
        }
      }

      if (offerParam) {
        const parsedOffer = Number(offerParam);
        if (!isNaN(parsedOffer) && parsedOffer > 0) {
          this.offerId = parsedOffer;
          this.infoMessage = `Recherche d'une réservation en attente pour l'offre #${parsedOffer}.`;
          this.loadBookingsForOffer(parsedOffer);
          return;
        }
      }

      this.infoMessage = 'Indiquez un identifiant de réservation ou d’offre pour continuer.';
    });
  }

  ngOnDestroy(): void {
    this.paramsSub?.unsubscribe();
  }

  private loadBookingByReservation(reservationId: number): void {
    this.isLoading = true;
    this.bookingsService.getBookingById(reservationId).subscribe({
      next: booking => {
        this.isLoading = false;
        if (this.isEligibleForRetry(booking)) {
          this.selectedBooking = booking;
          this.reservationId = booking.reservationId ?? reservationId;
        } else {
          this.errorMessage = 'Cette réservation n’est pas éligible à une relance de paiement.';
          this.selectedBooking = null;
        }
      },
      error: () => {
        this.isLoading = false;
        this.errorMessage = 'Impossible de retrouver la réservation. Consultez vos réservations pour relancer le paiement.';
      }
    });
  }

  private loadBookingsForOffer(offerId: number): void {
    this.isLoading = true;
    this.bookingsService.getMyBookings().subscribe({
      next: bookings => {
        this.isLoading = false;
        const matching = bookings.filter(b => (b.offerId === offerId) && this.isEligibleForRetry(b));
        if (matching.length === 0) {
          this.infoMessage = 'Aucune réservation éligible sur cette offre. Vérifiez votre historique.';
          this.selectedBooking = null;
          return;
        }
        this.bookingOptions = matching;
        this.selectBooking(matching[0]);
      },
      error: () => {
        this.isLoading = false;
        this.errorMessage = 'Impossible de charger vos réservations pour cette offre.';
      }
    });
  }

  private isEligibleForRetry(booking: Booking): boolean {
    const allowedStatuses = ['PENDING', 'REQUIRES_ACTION', 'FAILED', 'EXPIRED'];
    return allowedStatuses.includes((booking.paymentStatus || 'PENDING').toUpperCase());
  }

  selectBooking(booking: Booking): void {
    this.selectedBooking = booking;
    this.reservationId = booking.reservationId ?? null;
  }

  retryPayment(): void {
    if (!this.reservationId) {
      this.errorMessage = 'Aucune réservation sélectionnée.';
      return;
    }
    this.retrying = true;
    this.paymentService.startCheckout(this.reservationId).catch(error => {
      console.error('Retry paiement échoué', error);
      this.retrying = false;
      this.errorMessage = error?.message || 'Relance impossible pour le moment.';
    });
  }

  gotoBookings(): void {
    this.router.navigate(['/bookings']);
  }
}
