import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { NotificationService as NotificationStreamService } from '../../core/services/notification/notification';
import { NotificationService as ToastNotificationService } from '../../core/services/notification.service';
import { Notification as NotificationPayload } from '../../core/models/notification/notification.model';
import { AuthService } from '../../core/services/auth.service';
import { Toast } from '../../core/models/toast.model';

@Component({
  selector: 'app-notification',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notification.html',
  styleUrl: './notification.scss'
})
export class NotificationComponent implements OnInit, OnDestroy {
  notifications: NotificationPayload[] = [];
  unreadCount: number = 0;
  isPanelOpen: boolean = false;
  isLoading: boolean = false;
  private currentUserId?: string;
  
  private subscription?: Subscription;

  constructor(
    private notificationStreamService: NotificationStreamService,
    private authService: AuthService,
    private toastService: ToastNotificationService
  ) {}

  ngOnInit(): void {
    this.currentUserId = this.getCurrentUserId();
    this.loadExistingNotifications();
    this.connectToRealtimeNotifications();
  }

  private async loadExistingNotifications(): Promise<void> {
  this.isLoading = true;
  
  try {
    const userId = this.getCurrentUserId();
    if (userId) {
      const notifications = await this.notificationStreamService.getUserNotifications(userId);
      this.notifications = notifications;
      this.updateUnreadCount();
      console.log('📥 Notifications existantes chargées:', notifications.length);
    }
  } catch (error) {
    console.error('❌ Erreur chargement notifications existantes:', error);
  } finally {
    this.isLoading = false;
  }
}



async markAsRead(notification: NotificationPayload): Promise<void> {
  if (notification.read) return;
  
  notification.read = true;
  this.updateUnreadCount();
  
  if (notification.id) {
    await this.notificationStreamService.markAsRead(notification.id);
  }
}

  private connectToRealtimeNotifications(): void {
    const userId = this.currentUserId ?? this.getCurrentUserId();

    if (!userId) {
      console.warn('NotificationComponent: utilisateur non connecté, flux SSE non démarré.');
      return;
    }

    this.subscription = this.notificationStreamService.connect(userId).subscribe({
      next: (notification: NotificationPayload) => {
        console.log('📩 Nouvelle notification reçue:', notification);
        this.handleRealtimeNotification(notification);
      },
      error: (error) => {
        console.error('💥 Erreur dans le flux de notifications:', error);
      }
    });
  }

  private handleRealtimeNotification(notification: NotificationPayload): void {
    this.addNotification(notification);
    this.updateUnreadCount();
    if (this.shouldDisplayToast(notification)) {
      this.showToastFromNotification(notification);
    }
  }

  private addNotification(notification: NotificationPayload): void {
    this.notifications.unshift(notification);
    
    // Garder seulement les 50 dernières notifications
    if (this.notifications.length > 50) {
      this.notifications = this.notifications.slice(0, 50);
    }
  }

  private updateUnreadCount(): void {
    this.unreadCount = this.notifications.filter(notification => !notification.read).length;
  }

  private getCurrentUserId(): string {
    const currentUser = this.authService.currentUserValue;

    if (currentUser?.id !== undefined && currentUser?.id !== null) {
      return String(currentUser.id);
    }

    try {
      const storedUser = localStorage.getItem('currentUser');
      if (storedUser) {
        const parsed = JSON.parse(storedUser);
        if (parsed?.id !== undefined && parsed?.id !== null) {
          return String(parsed.id);
        }
      }
    } catch (error) {
      console.error('NotificationComponent: impossible de récupérer le currentUser depuis le stockage', error);
    }

    return '';
  }

  // ✅ Méthodes pour l'interface utilisateur
  toggleNotificationPanel(): void {
    this.isPanelOpen = !this.isPanelOpen;
    if (this.isPanelOpen) {
      this.markAllAsRead();
    }
  }

 async markAllAsRead(): Promise<void> {
  const userId = this.getCurrentUserId();
  if (!userId) return;
  
  this.notifications.forEach(notification => {
    notification.read = true;
  });
  this.unreadCount = 0;
  
  await this.notificationStreamService.markAllAsRead(userId);
}

 async removeNotification(notification: NotificationPayload): Promise<void> {
  if (notification.id) {
    await this.notificationStreamService.deleteNotification(notification.id);
  }
  
  this.notifications = this.notifications.filter(n => n.id !== notification.id);
  this.updateUnreadCount();
}
  getNotificationIcon(notification: NotificationPayload): string {
    switch (notification.type) {
      case 'SUCCESS': return '✅';
      case 'ERROR': return '❌';
      case 'WARNING': return '⚠️';
      case 'URGENT': return '🚨';
      default: return 'ℹ️';
    }
  }

  getNotificationClass(notification: NotificationPayload): string {
    return `notification-item notification-${notification.type.toLowerCase()}`;
  }

  // ✅ Méthodes de test existantes
  sendTest(): void {
    this.notificationStreamService.sendTestNotification();
  }

  sendCustomTest(type: string): void {
    const messages = {
      'success': 'Opération réussie ! ✅',
      'error': 'Une erreur est survenue ❌', 
      'warning': 'Attention ⚠️',
      'info': 'Information ℹ️'
    };
    
    const message = messages[type as keyof typeof messages] || 'Test message';
    const severity = type.toUpperCase() as NotificationPayload['type'];

    this.notificationStreamService.sendCustomTest(message);
  }

  clearNotifications(): void {
    this.notifications = [];
    this.unreadCount = 0;
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
    this.notificationStreamService.disconnect();
  }

  private showToastFromNotification(notification: NotificationPayload): void {
    const toastType = this.mapSeverityToToastType(notification.severity ?? notification.type);
    const metadata = notification.metadata ?? {};
    const eventType = this.getMetadataValue(metadata, 'eventType')?.toLowerCase();
    const entityType = this.getMetadataValue(metadata, 'entityType')?.toLowerCase();
    const paymentStatus = this.getMetadataValue(metadata, 'paymentStatus')?.toLowerCase();
    const reservationId = this.getMetadataValue(metadata, 'reservationId');
    const offerTitle =
      this.getMetadataValue(metadata, 'offerTitle') ??
      this.getMetadataValue(metadata, 'offerDescription') ??
      this.extractOfferFromMessage(notification);
    const systemEvent = this.getMetadataValue(metadata, 'systemEvent')?.toLowerCase();

    const message = this.buildToastMessage({
      notification,
      eventType,
      entityType,
      paymentStatus,
      reservationId,
      offerTitle,
      systemEvent
    });

    const sanitizedMessage = this.sanitizeMessage(message);

    if (sanitizedMessage) {
      this.toastService.show(sanitizedMessage, toastType);
    }
  }

  private shouldDisplayToast(notification: NotificationPayload): boolean {
    if (!notification) {
      return false;
    }

    // Ignorer le toast envoyé lors de la connexion SSE
    if (
      notification.category?.toUpperCase() === 'SYSTEM_ALERT' &&
      (notification.title ?? '').toLowerCase().includes('connexion')
    ) {
      return false;
    }

    const metadata = notification.metadata ?? {};
    const eventType = this.getMetadataValue(metadata, 'eventType')?.toLowerCase();
    const entityType = this.getMetadataValue(metadata, 'entityType')?.toLowerCase();

    return Boolean(
      this.isPaymentRelated(eventType, entityType, this.getMetadataValue(metadata, 'paymentStatus')?.toLowerCase()) ||
      this.isReservationRelated(eventType, entityType) ||
      eventType === 'favorite_added' ||
      eventType === 'favorite_removed' ||
      eventType === 'offer_available' ||
      notification.category?.toUpperCase() === 'BUSINESS_EVENT' ||
      notification.category?.toUpperCase() === 'SECURITY_ALERT' ||
      notification.category?.toUpperCase() === 'SYSTEM_ALERT'
    );
  }

  private buildToastMessage(context: {
    notification: NotificationPayload;
    eventType?: string;
    entityType?: string;
    paymentStatus?: string;
    reservationId?: string;
    offerTitle?: string;
    systemEvent?: string;
  }): string | undefined {
    const { notification, eventType, entityType, paymentStatus, reservationId, offerTitle, systemEvent } = context;

    if (notification.category?.toUpperCase() === 'SYSTEM_ALERT') {
      if (systemEvent === 'maintenance') {
        return 'Une maintenance planifiée va débuter prochainement.';
      }
      return notification.content ?? notification.title;
    }

    if (this.isPaymentRelated(eventType, entityType, paymentStatus)) {
      return this.buildPaymentToastMessage(paymentStatus, eventType, reservationId);
    }

    if (this.isReservationRelated(eventType, entityType)) {
      return this.buildReservationToastMessage(eventType, reservationId);
    }

    if (eventType === 'offer_favorite' || eventType === 'favorite_added') {
      return offerTitle ? `Vous suivez désormais "${offerTitle}".` : 'Offre ajoutée à vos favoris.';
    }

    if (eventType === 'favorite_removed') {
      return offerTitle ? `"${offerTitle}" a été retirée de vos favoris.` : 'Offre retirée de vos favoris.';
    }

    if (eventType === 'offer_available' || eventType === 'offer_created') {
      return offerTitle ?? notification.content;
    }

    if (eventType === 'favorite_available') {
      return offerTitle
        ? `"${offerTitle}" est de nouveau disponible. Confirmez rapidement votre réservation !`
        : 'Une offre suivie est de nouveau disponible.';
    }

    if (eventType === 'payment_retry') {
      return reservationId
        ? `Paiement relancé pour la réservation #${reservationId}.`
        : 'Le paiement a été relancé.';
    }

    if (notification.category?.toUpperCase() === 'BUSINESS_EVENT') {
      return notification.content ?? notification.title;
    }

    if (notification.category?.toUpperCase() === 'SECURITY_ALERT') {
      return 'Activité de sécurité détectée sur votre compte. Vérifiez vos informations.';
    }

    return notification.content ?? notification.title;
  }

  private isPaymentRelated(eventType?: string, entityType?: string, paymentStatus?: string): boolean {
    return Boolean(
      paymentStatus ||
      entityType === 'payment' ||
      (eventType?.startsWith('payment') ?? false) ||
      eventType === 'refunded'
    );
  }

  private isReservationRelated(eventType?: string, entityType?: string): boolean {
    return Boolean(
      entityType === 'reservation' ||
      eventType === 'created' ||
      eventType === 'confirmed' ||
      eventType === 'cancelled' ||
      eventType === 'completed'
    );
  }

  private buildPaymentToastMessage(
    paymentStatus?: string,
    eventType?: string,
    reservationId?: string
  ): string {
    const label = reservationId ? ` pour la réservation #${reservationId}` : '';

    if (paymentStatus === 'pending' || eventType === 'payment_pending') {
      return `Un paiement${label} nécessite votre action. Relancez-le depuis votre espace.`;
    }

    if (paymentStatus === 'completed' || eventType === 'payment_success' || eventType === 'completed') {
      return `Paiement confirmé${label}.`;
    }

    if (paymentStatus === 'expired' || eventType === 'payment_expired') {
      return `Le paiement${label} a expiré. Relancez l'opération.`;
    }

    if (paymentStatus === 'refunded' || eventType === 'refunded') {
      return `Paiement remboursé${label}.`;
    }

    if (eventType === 'payment_retry') {
      return `Relance du paiement${label} en cours. Surveillez l'état de la transaction.`;
    }

    if (eventType === 'payment_failed' || !paymentStatus) {
      return `Le paiement${label} a échoué.`;
    }

    return `Mise à jour du paiement${label}.`;
  }

  private buildReservationToastMessage(eventType?: string, reservationId?: string): string | undefined {
    const label = reservationId ? `Réservation #${reservationId}` : 'Votre réservation';

    switch (eventType) {
      case 'created':
        return `${label} est enregistrée.`;
      case 'confirmed':
        return `${label} est confirmée.`;
      case 'cancelled':
        return `${label} a été annulée.`;
      case 'completed':
        return `${label} est terminée. Merci pour votre utilisation !`;
      default:
        return undefined;
    }
  }

  private mapSeverityToToastType(severity: NotificationPayload['type']): Toast['type'] {
    switch (severity) {
      case 'SUCCESS':
        return 'success';
      case 'ERROR':
      case 'URGENT':
        return 'error';
      case 'WARNING':
        return 'warning';
      default:
        return 'info';
    }
  }

  private getMetadataValue(metadata: Record<string, unknown>, key: string): string | undefined {
    const value = metadata[key];
    if (typeof value === 'string') {
      return value;
    }
    if (value !== undefined && value !== null) {
      return String(value);
    }
    return undefined;
  }

  private extractOfferFromMessage(notification: NotificationPayload): string | undefined {
    const match = notification.content?.match(/«([^»]+)»|\"([^"]+)\"/);
    if (match) {
      return match[1] || match[2];
    }
    return notification.title;
  }

  private sanitizeMessage(message?: string): string | undefined {
    if (!message) {
      return undefined;
    }
    return message.replace(/\s+/g, ' ').trim();
  }
}
