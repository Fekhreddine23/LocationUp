import { Injectable } from '@angular/core';
import { environment } from '../../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class BusinessEventsService {

   private readonly baseUrl = environment.apiUrl ?? 'http://localhost:8088';

     constructor(private http: HttpClient) {}

  private postEvent(endpoint: string, params: Record<string, string>): Observable<void> {
    return this.http.post(`${this.baseUrl}${endpoint}`, null, {
      params,
      responseType: 'text' as 'json'
    }).pipe(map(() => void 0));
  }

  // === ÉVÉNEMENTS RÉSERVATIONS ===
  notifyBookingCreated(bookingId: number, userId: number, offerDetails?: any) {
    const params: Record<string, string> = {
      reservationId: bookingId.toString(),
      userId: userId.toString(),
      eventType: 'CREATED'
    };

    if (offerDetails) {
      params['offerDescription'] = offerDetails.description;
      if (offerDetails.price !== undefined) {
        params['price'] = offerDetails.price.toString();
      }
    }

    return this.postEvent('/api/notifications/reservation-event', params);
  }

  notifyBookingConfirmed(bookingId: number, userId: number) {
    return this.postEvent('/api/notifications/reservation-event', {
      reservationId: bookingId.toString(),
      userId: userId.toString(),
      eventType: 'CONFIRMED'
    });
  }

  notifyBookingCancelled(bookingId: number, userId: number, reason?: string) {
    return this.postEvent('/api/notifications/reservation-event', {
      reservationId: bookingId.toString(),
      userId: userId.toString(),
      eventType: 'CANCELLED',
      reason: reason || 'Annulé par l\'utilisateur'
    });
  }

  notifyBookingCompleted(bookingId: number, userId: number) {
    return this.postEvent('/api/notifications/reservation-event', {
      reservationId: bookingId.toString(),
      userId: userId.toString(),
      eventType: 'COMPLETED'
    });
  }

  // === ÉVÉNEMENTS OFFRES ===
  notifyOfferCreated(offerId: number, description: string, adminId: number) {
    return this.postEvent('/api/notifications/system-event', {
      eventType: 'OFFER_CREATED',
      message: `Nouvelle offre créée: "${description.substring(0, 50)}..."`,
      severity: 'INFO',
      userId: adminId.toString()
    });
  }

  notifyOfferAvailable(offerId: number, description: string, userId: number) {
    return this.postEvent('/api/notifications/system-event', {
      eventType: 'OFFER_AVAILABLE',
      message: `Nouvelle offre disponible: "${description.substring(0, 50)}..."`,
      severity: 'INFO',
      userId: userId.toString()
    });
  }


  /**
 * Notifier un événement système générique
 */
notifySystemEvent(
  eventType: string, 
  message: string, 
  severity: 'SUCCESS' | 'ERROR' | 'WARNING' | 'INFO',
  userId: number
) {
  return this.postEvent('/api/notifications/system-event', {
    eventType,
    message,
    severity,
    userId: userId.toString()
  });
}

}
