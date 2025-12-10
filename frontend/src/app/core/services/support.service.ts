import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BusinessEventsService } from './business-events/business-events';
import { AuthService } from './auth.service';

export interface SupportRequestPayload {
  subject: string;
  category: string;
  relatedId?: string;
  message: string;
  urgency: 'normal' | 'urgent';
  contactEmail?: string;
}

@Injectable({
  providedIn: 'root'
})
export class SupportService {

  constructor(
    private businessEvents: BusinessEventsService,
    private authService: AuthService
  ) {}

  submitRequest(payload: SupportRequestPayload): Observable<void> {
    const user = this.authService.currentUserValue;
    const userId = user?.id ?? 0;
    const formattedMessage = [
      `Categorie: ${payload.category}`,
      `Sujet: ${payload.subject}`,
      `Urgence: ${payload.urgency}`,
      payload.relatedId ? `Référence liée: ${payload.relatedId}` : null,
      payload.contactEmail ? `Contact: ${payload.contactEmail}` : null,
      '',
      payload.message
    ].filter(Boolean).join('\n');

    return this.businessEvents.notifySystemEvent(
      'SUPPORT_REQUEST',
      formattedMessage,
      'INFO',
      userId
    );
  }
}
