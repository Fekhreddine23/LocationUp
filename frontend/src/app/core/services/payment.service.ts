import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { lastValueFrom } from 'rxjs';
import { environment } from '../../../environments/environment';

interface PaymentSessionResponse {
  paymentUrl: string;
  sessionId: string;
}

@Injectable({ providedIn: 'root' })
export class PaymentService {

  constructor(private http: HttpClient) {}

  async startCheckout(reservationId: number, successUrl?: string, cancelUrl?: string): Promise<void> {
    const payload = {
      reservationId,
      successUrl: successUrl ?? `${window.location.origin}/payments/success?reservationId=${reservationId}`,
      cancelUrl: cancelUrl ?? `${window.location.origin}/payments/cancel?reservationId=${reservationId}`
    };

    const session$ = this.http.post<PaymentSessionResponse>(`${environment.apiUrl}/api/payments/session`, payload);
    const response = await lastValueFrom(session$);

    if (response.paymentUrl) {
      window.location.href = response.paymentUrl;
      return;
    }

    throw new Error('Paiement indisponible');
  }
}
