import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { NotificationService } from '../../core/services/notification/notification';
import { Notification as NotificationPayload } from '../../core/models/notification/notification.model';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-notification',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notification.html',
  styleUrl: './notification.scss'
})
export class NotificationComponent implements OnInit, OnDestroy {

  notifications: NotificationPayload[] = [];
  private subscription?: Subscription;

  constructor(
    private notificationService: NotificationService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const userId = this.getCurrentUserId();

    if (!userId) {
      console.warn('NotificationComponent: utilisateur non connecté, flux SSE non démarré.');
      return;
    }

    this.subscription = this.notificationService.connect(userId).subscribe(
      (notification: NotificationPayload) => {
        this.notifications.unshift(notification);
        // Garder seulement les 10 dernières notifications
        if (this.notifications.length > 10) {
          this.notifications.pop();
        }
      }
    );
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

  sendTest(): void {
    this.notificationService.sendTestNotification();
  }

  // ✅ Nouvelle méthode pour envoyer différents types de tests
  sendCustomTest(type: string): void {
    const messages = {
      'success': 'Opération réussie ! ✅',
      'error': 'Une erreur est survenue ❌', 
      'warning': 'Attention ⚠️',
      'info': 'Information ℹ️'
    };
    
    const message = messages[type as keyof typeof messages] || 'Test message';
    const severity = type.toUpperCase() as NotificationPayload['type'];

    this.notificationService.sendCustomTest(message);
  }

  // ✅ Nettoyer les notifications
  clearNotifications(): void {
    this.notifications = [];
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
    this.notificationService.disconnect();
  }
}
