import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { BookingsService, Booking } from '../../core/services/bookings';
import { PaymentService } from '../../core/services/payment.service';

@Component({
  selector: 'app-payment-cancel',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
  <div class="payment-feedback cancel">
    <h1>⚠️ Paiement annulé</h1>
    <p>La transaction a été interrompue avant validation.</p>

    <div *ngIf="isLoading" class="loading-state">
      <div class="spinner"></div>
      <p>Récupération des détails de la réservation...</p>
    </div>

    <div *ngIf="errorMessage" class="alert">
      {{ errorMessage }}
    </div>

    <ng-container *ngIf="booking">
      <div class="booking-recap">
        <div class="recap-row">
          <span>Réservation</span>
          <strong>#{{ booking.reservationId }}</strong>
        </div>
        <div class="recap-row">
          <span>Date du trajet</span>
          <strong>{{ booking.reservationDate | date:'dd/MM/yyyy à HH:mm' }}</strong>
        </div>
        <div class="recap-row">
          <span>Statut actuel</span>
          <strong>{{ booking.status }}</strong>
        </div>
        <div class="recap-row">
          <span>Statut paiement</span>
          <strong>{{ booking.paymentStatus || 'PENDING' }}</strong>
        </div>
      </div>
    </ng-container>

    <div class="actions">
      <button class="btn secondary" type="button" (click)="gotoBookings()">
        📋 Voir mes réservations
      </button>
      <button class="btn primary" type="button"
              [disabled]="retrying || !reservationId"
              (click)="retryPayment()">
        {{ retrying ? 'Relance en cours...' : '💳 Relancer le paiement' }}
      </button>
    </div>
  </div>
  `,
  styleUrls: ['./payment-feedback.scss']
})
export class PaymentCancelComponent implements OnInit {
  reservationId: number | null = null;
  booking: Booking | null = null;
  isLoading = false;
  retrying = false;
  errorMessage = '';

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private bookingsService: BookingsService,
    private paymentService: PaymentService
  ) {}

  ngOnInit(): void {
    this.route.queryParamMap.subscribe(params => {
      const idParam = params.get('reservationId');
      const reservationId = idParam ? Number(idParam) : NaN;
      if (!isNaN(reservationId) && reservationId > 0) {
        this.reservationId = reservationId;
        this.fetchBooking(reservationId);
      } else {
        this.errorMessage = 'Impossible d’identifier la réservation annulée.';
      }
    });
  }

  private fetchBooking(reservationId: number): void {
    this.isLoading = true;
    this.bookingsService.getBookingById(reservationId).subscribe({
      next: booking => {
        this.booking = booking;
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Impossible de récupérer la réservation. Rendez-vous dans “Mes réservations”.';
        this.isLoading = false;
      }
    });
  }

  retryPayment(): void {
    if (!this.reservationId) {
      this.errorMessage = 'Aucune réservation à relancer.';
      return;
    }
    this.retrying = true;
    this.paymentService.startCheckout(this.reservationId).catch(error => {
      this.retrying = false;
      this.errorMessage = error?.message || 'Impossible de relancer le paiement pour le moment.';
    });
  }

  gotoBookings(): void {
    this.router.navigate(['/bookings']);
  }
}
