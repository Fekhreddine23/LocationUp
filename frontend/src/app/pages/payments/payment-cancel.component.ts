import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-payment-cancel',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
  <div class="payment-feedback cancel">
    <h1>⚠️ Paiement annulé</h1>
    <p>La transaction a été interrompue. Vous pouvez réessayer ou contacter le support si besoin.</p>
    <button (click)="retry()">Réessayer</button>
  </div>
  `,
  styleUrls: ['./payment-feedback.scss']
})
export class PaymentCancelComponent {
  constructor(private router: Router) {}

  retry(): void {
    this.router.navigate(['/bookings/new']);
  }
}
