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
  unreadCount: number = 0;
  isPanelOpen: boolean = false;
  isLoading: boolean = false;
  
  private subscription?: Subscription;

  constructor(
    private notificationService: NotificationService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadExistingNotifications();
    this.connectToRealtimeNotifications();
  }

  private async loadExistingNotifications(): Promise<void> {
  this.isLoading = true;
  
  try {
    const userId = this.getCurrentUserId();
    if (userId) {
      const notifications = await this.notificationService.getUserNotifications(userId);
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
    await this.notificationService.markAsRead(notification.id);
  }
}

  private connectToRealtimeNotifications(): void {
    const userId = this.getCurrentUserId();

    if (!userId) {
      console.warn('NotificationComponent: utilisateur non connecté, flux SSE non démarré.');
      return;
    }

    this.subscription = this.notificationService.connect(userId).subscribe(
      (notification: NotificationPayload) => {
        console.log('📩 Nouvelle notification reçue:', notification);
        this.addNotification(notification);
        this.updateUnreadCount();
      },
      (error) => {
        console.error('💥 Erreur dans le flux de notifications:', error);
      }
    );
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
  
  await this.notificationService.markAllAsRead(userId);
}

 async removeNotification(notification: NotificationPayload): Promise<void> {
  if (notification.id) {
    await this.notificationService.deleteNotification(notification.id);
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
    this.notificationService.sendTestNotification();
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

    this.notificationService.sendCustomTest(message);
  }

  clearNotifications(): void {
    this.notifications = [];
    this.unreadCount = 0;
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
    this.notificationService.disconnect();
  }
}