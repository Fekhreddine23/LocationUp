import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-payment-success',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
  <div class="payment-feedback success">
    <h1>🎉 Paiement confirmé</h1>
    <p>Votre transaction a été validée. Un e-mail de confirmation vous a été envoyé.</p>
    <button (click)="goToBookings()">Voir mes réservations</button>
  </div>
  `,
  styleUrls: ['./payment-feedback.scss']
})
export class PaymentSuccessComponent {
  constructor(private router: Router) {}

  goToBookings(): void {
    this.router.navigate(['/bookings']);
  }
}
