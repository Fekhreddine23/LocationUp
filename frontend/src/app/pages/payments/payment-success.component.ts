import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { BookingsService, Booking } from '../../core/services/bookings';
import { PaymentService } from '../../core/services/payment.service';

@Component({
  selector: 'app-payment-success',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
  <div class="payment-feedback success">
    <h1>🎉 Paiement confirmé</h1>
    <p>Votre transaction a été validée. Un e-mail de confirmation vous a été envoyé.</p>

    <div *ngIf="isLoading" class="loading-state">
      <div class="spinner"></div>
      <p>Récupération de votre réservation...</p>
    </div>
    <div *ngIf="syncingPayment" class="loading-state subtle">
      <div class="spinner"></div>
      <p>Vérification du paiement en cours...</p>
    </div>

    <div *ngIf="errorMessage" class="alert alert-warning">
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
          <span>Statut</span>
          <strong>{{ booking.status }}</strong>
        </div>
        <div class="recap-row">
          <span>Statut paiement</span>
          <strong>{{ getPaymentStatusLabel(booking.paymentStatus) }}</strong>
        </div>
      </div>

      <div class="actions">
        <button class="btn secondary" type="button" (click)="goToTimeline()">
          📅 Consulter la timeline
        </button>
        <button 
          class="btn secondary"
          type="button"
          (click)="downloadReceipt()"
          [disabled]="receiptLoading">
          {{ receiptLoading ? 'Téléchargement...' : '🧾 Télécharger le reçu' }}
        </button>
        <button class="btn primary" type="button" (click)="goToBookings()">
          📋 Voir mes réservations
        </button>
      </div>
    </ng-container>
  </div>
  `,
  styleUrls: ['./payment-feedback.scss']
})
export class PaymentSuccessComponent implements OnInit {
  reservationId: number | null = null;
  booking: Booking | null = null;
  isLoading = false;
  errorMessage = '';
  receiptLoading = false;
  syncingPayment = false;

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
        this.syncPaymentStatus(reservationId);
      } else {
        this.errorMessage = 'Impossible de retrouver la réservation. Visitez vos réservations pour vérifier le paiement.';
      }
    });
  }

  private syncPaymentStatus(reservationId: number): void {
    this.syncingPayment = true;
    this.paymentService.syncPaymentStatus(reservationId).subscribe({
      next: () => {
        this.syncingPayment = false;
        this.fetchBooking(reservationId);
      },
      error: (error) => {
        console.error('Payment sync failed', error);
        this.syncingPayment = false;
        this.fetchBooking(reservationId);
      }
    });
  }

  private fetchBooking(reservationId: number): void {
    this.isLoading = true;
    this.bookingsService.getBookingById(reservationId).subscribe({
      next: (booking) => {
        this.booking = booking;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading booking', error);
        this.errorMessage = 'Impossible de charger le récapitulatif de la réservation.';
        this.isLoading = false;
      }
    });
  }

  downloadReceipt(): void {
    if (!this.reservationId) {
      return;
    }
    this.receiptLoading = true;
    this.bookingsService.downloadReceipt(this.reservationId).subscribe({
      next: (blob) => {
        this.receiptLoading = false;
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `recu-reservation-${this.reservationId}.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: (error) => {
        console.error('Receipt download failed', error);
        this.errorMessage = 'Téléchargement impossible pour le moment. Réessayez plus tard.';
        this.receiptLoading = false;
      }
    });
  }

  goToTimeline(): void {
    if (this.reservationId) {
      this.router.navigate(['/bookings'], { queryParams: { focus: this.reservationId } });
    } else {
      this.goToBookings();
    }
  }

  getPaymentStatusLabel(status?: string): string {
    if (!status) {
      return 'Paiement en attente';
    }
    const map: { [key: string]: string } = {
      PENDING: 'Paiement en attente',
      REQUIRES_ACTION: 'Action requise',
      PAID: 'Payé',
      FAILED: 'Échec du paiement',
      REFUNDED: 'Remboursé',
      EXPIRED: 'Paiement expiré'
    };
    return map[status.toUpperCase()] || status;
  }

  goToBookings(): void {
    this.router.navigate(['/bookings']);
  }
}
