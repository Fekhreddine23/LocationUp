import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Notification as NotificationPayload } from '../../models/notification/notification.model';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private eventSource?: EventSource;
  private readonly baseUrl = environment.apiUrl ?? 'http://localhost:8088';
  private currentUserId?: string;
  private reconnectionAttempts = 0;
  private maxReconnectionAttempts = 5;

  connect(userId: string): Observable<NotificationPayload> {
    this.currentUserId = userId;

    return new Observable<NotificationPayload>(observer => {
      const streamUrl = `${this.baseUrl}/api/notifications/stream?userId=${encodeURIComponent(userId)}`;

      const startEventSource = () => {
        if (this.eventSource) {
          this.eventSource.close();
        }

        console.log('🔄 Connexion SSE vers:', streamUrl);
        this.eventSource = new EventSource(streamUrl);

        this.eventSource.onopen = () => {
          console.log('✅ Connexion SSE établie');
          this.reconnectionAttempts = 0; // Reset des tentatives en cas de succès
        };

        // ✅ CORRECTION : Écouter les événements nommés "notification"
        this.eventSource.addEventListener('notification', (event: MessageEvent) => {
          console.log('📨 Événement "notification" reçu:', event);
          try {
            const rawPayload = this.safeParse(event.data);
            console.log('🔍 Payload parsé:', rawPayload);

            const notification = this.mapToNotificationModel(rawPayload);
            console.log('🎯 Notification mappée:', notification);

            observer.next(notification);
          } catch (error) {
            console.error('❌ Erreur traitement événement:', error);
          }
        });

        // ✅ CONSERVER aussi onmessage pour les événements sans nom
        this.eventSource.onmessage = (event) => {
          console.log('📨 Événement générique reçu:', event.data);
          try {
            const rawPayload = this.safeParse(event.data);
            const notification = this.mapToNotificationModel(rawPayload);
            observer.next(notification);
          } catch (error) {
            console.error('❌ Erreur traitement message générique:', error);
          }
        };

        this.eventSource.onerror = error => {
          console.error('❌ Erreur flux SSE notifications', error);

          this.reconnectionAttempts++;
          const shouldAttemptReconnect =
            this.currentUserId === userId &&
            this.reconnectionAttempts <= this.maxReconnectionAttempts;

          if (shouldAttemptReconnect) {
            const delay = Math.min(3000 * this.reconnectionAttempts, 30000);
            console.warn(`♻️ Tentative de reconnexion SSE ${this.reconnectionAttempts}/${this.maxReconnectionAttempts} dans ${delay}ms...`);

            this.disconnect(false);
            setTimeout(() => {
              if (this.currentUserId === userId && !this.eventSource) {
                startEventSource();
              }
            }, delay);
          } else {
            console.error('💥 Nombre maximum de tentatives de reconnexion atteint');
            observer.error(new Error('Connexion SSE perdue après plusieurs tentatives'));
          }
        };
      };

      startEventSource();

      return () => {
        console.log('🔌 Nettoyage Observable SSE');
        this.disconnect(true);
      };
    });
  }

  sendTestNotification(message: string = 'Test notification', severity: NotificationPayload['type'] = 'INFO'): void {
    if (!this.currentUserId) {
      console.error('❌ UserId non défini. Appelez connect() au préalable.');
      return;
    }

    const requestUrl = `${this.baseUrl}/api/notifications/test?userId=${encodeURIComponent(this.currentUserId)}&message=${encodeURIComponent(message)}&severity=${severity.toUpperCase()}`;

    console.log('📤 Envoi test vers:', requestUrl);

    fetch(requestUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      }
    })
      .then(response => {
        console.log('📝 Réponse HTTP:', response.status, response.statusText);
        if (response.ok) {
          console.log('✅ Notification de test envoyée');
          return response.text();
        } else {
          console.error(`❌ Erreur envoi notification de test: HTTP ${response.status}`);
          return response.text().then(text => { throw new Error(text); });
        }
      })
      .then(text => {
        console.log('📄 Contenu réponse:', text);
      })
      .catch(error => {
        console.error('❌ Erreur réseau lors de l\'envoi de la notification de test', error);
      });
  }

  sendCustomNotification(payload: {
    message: string;
    severity?: NotificationPayload['type'];
    title?: string;
    category?: string;
    metadata?: Record<string, unknown>
  }): void {
    if (!this.currentUserId) {
      console.error('❌ UserId non défini. Appelez connect() au préalable.');
      return;
    }

    const requestPayload = {
      category: (payload.category ?? 'USER_ACTION').toUpperCase(),
      severity: (payload.severity ?? 'INFO').toUpperCase(),
      title: payload.title ?? 'Notification',
      message: payload.message,
      recipient: this.currentUserId,
      metadata: payload.metadata ?? {}
    };

    console.log('📤 Envoi notification personnalisée:', requestPayload);

    fetch(`${this.baseUrl}/api/notifications/send`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      },
      body: JSON.stringify(requestPayload)
    })
      .then(response => {
        console.log('📝 Réponse personnalisée:', response.status);
        if (response.ok) {
          console.log('✅ Notification personnalisée envoyée');
          return response.text();
        } else {
          console.error(`❌ Erreur envoi notification personnalisée: HTTP ${response.status}`);
          return response.text().then(text => { throw new Error(text); });
        }
      })
      .then(text => {
        console.log('📄 Réponse personnalisée texte:', text);
      })
      .catch(error => {
        console.error('❌ Erreur réseau lors de l\'envoi de la notification personnalisée', error);
      });
  }

  sendCustomTest(message: string, severity?: NotificationPayload['type']): void;
  sendCustomTest(options?: { message?: string; severity?: NotificationPayload['type'] }): void;
  sendCustomTest(
    messageOrOptions: string | { message?: string; severity?: NotificationPayload['type'] } = {},
    severityArg?: NotificationPayload['type']
  ): void {
    let message: string;
    let severity: NotificationPayload['type'];

    if (typeof messageOrOptions === 'string') {
      message = messageOrOptions;
      severity = severityArg ?? 'INFO';
    } else {
      message = messageOrOptions.message ?? 'Test notification';
      severity = messageOrOptions.severity ?? 'INFO';
    }

    console.log('🧪 Envoi test personnalisé:', { message, severity });
    this.sendTestNotification(message, severity);
  }

  disconnect(clearUserId: boolean = true): void {
    if (this.eventSource) {
      console.log('🔌 Fermeture connexion SSE');
      this.eventSource.close();
      this.eventSource = undefined;
    }
    if (clearUserId) {
      this.currentUserId = undefined;
    }
    this.reconnectionAttempts = 0;
  }

  getCurrentUserId(): string | undefined {
    return this.currentUserId;
  }

  private mapToNotificationModel(raw: any): NotificationPayload {
    // ✅ CORRECTION : Mapping amélioré basé sur le format backend
    const type = this.getNotificationType(raw);
    const content = raw.message || raw.content || raw.title || 'Notification sans message';
    const timestamp = raw.createdAt || raw.timestamp || new Date().toISOString();
    const recipient = raw.recipient || this.currentUserId || 'unknown';

    return {
      type,
      content,
      timestamp,
      recipient,
      id: raw.id ? String(raw.id) : undefined, // Convertir en string si numérique
      category: this.getNotificationCategory(raw),
      severity: this.getNotificationSeverity(raw),
      title: typeof raw.title === 'string' ? raw.title : undefined,
      metadata: this.normalizeMetadata(raw.metadata),
      read: typeof raw.read === 'boolean' ? raw.read : false,
      dismissible: typeof raw.dismissible === 'boolean' ? raw.dismissible : true,
      actionUrl: typeof raw.actionUrl === 'string' ? raw.actionUrl : undefined,
      actionLabel: typeof raw.actionLabel === 'string' ? raw.actionLabel : undefined,
      expiresAt: typeof raw.expiresAt === 'string' ? raw.expiresAt : undefined
    };
  }

  private safeParse(data: string): Record<string, unknown> {
    if (!data) {
      return {};
    }

    try {
      return JSON.parse(data);
    } catch (error) {
      console.warn('NotificationService: payload non JSON reçu, passage en mode texte.', data);
      return {
        message: data,
        severity: 'INFO',
        createdAt: new Date().toISOString(),
        recipient: this.currentUserId ?? 'system'
      };
    }
  }

  private getNotificationType(raw: any): NotificationPayload['type'] {
    const severity = (raw?.severity ?? raw?.type ?? 'INFO').toString().toUpperCase();

    switch (severity) {
      case 'SUCCESS':
      case 'ERROR':
      case 'WARNING':
      case 'INFO':
      case 'URGENT':
        return severity;
      default:
        return 'INFO';
    }
  }

  private getNotificationSeverity(raw: any): NotificationPayload['type'] | undefined {
    if (!raw?.severity && !raw?.type) {
      return undefined;
    }
    const severity = (raw.severity ?? raw.type ?? '').toString().toUpperCase();
    return this.getNotificationType({ severity });
  }

  private getNotificationCategory(raw: any): string {
    if (raw?.category) {
      return raw.category.toString();
    }
    if (raw?.topic) {
      return raw.topic.toString();
    }
    return 'GENERAL';
  }

  private normalizeMetadata(metadata: unknown): Record<string, unknown> | undefined {
    if (!metadata) {
      return undefined;
    }

    if (typeof metadata === 'string') {
      try {
        return JSON.parse(metadata);
      } catch {
        return { value: metadata };
      }
    }

    if (typeof metadata === 'object') {
      return metadata as Record<string, unknown>;
    }

    return undefined;
  }

  // ✅ NOUVEAU : Méthode pour tester la connexion rapidement
  quickTest(userId: string = '13'): void {
    console.log('🚀 Démarrage test rapide...');

    this.connect(userId).subscribe({
      next: (notification) => {
        console.log('🎉 NOTIFICATION REÇUE DANS TEST:', notification);
      },
      error: (error) => {
        console.error('💥 ERREUR DANS TEST:', error);
      },
      complete: () => {
        console.log('🏁 Test complet');
      }
    });

    // Envoyer un test après 1 seconde
    setTimeout(() => {
      console.log('🧪 Envoi notification test...');
      this.sendTestNotification('Test rapide depuis Angular', 'SUCCESS');
    }, 1000);

    // Envoyer un autre test après 3 secondes
    setTimeout(() => {
      console.log('🧪 Envoi deuxième notification test...');
      this.sendTestNotification('Deuxième test temps réel', 'INFO');
    }, 3000);
  }

  // ✅ NOUVEAU : Méthode pour vérifier l'état de la connexion
  getConnectionState(): string {
    if (!this.eventSource) {
      return 'DISCONNECTED';
    }

    switch (this.eventSource.readyState) {
      case EventSource.CONNECTING:
        return 'CONNECTING';
      case EventSource.OPEN:
        return 'CONNECTED';
      case EventSource.CLOSED:
        return 'CLOSED';
      default:
        return 'UNKNOWN';
    }
  }

  // ✅ NOUVEAU : Méthode pour forcer une reconnexion
  reconnect(): void {
    if (this.currentUserId) {
      console.log('🔄 Reconnexion forcée...');
      this.disconnect(false);
      this.connect(this.currentUserId);
    }
  }



  /**
 * Charge les notifications existantes pour un utilisateur
 */
  getUserNotifications(userId: string): Promise<NotificationPayload[]> {
    return fetch(`${this.baseUrl}/api/notifications/user/${userId}`)
      .then(response => {
        if (!response.ok) {
          throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        return response.json();
      })
      .then((notifications: any[]) => {
        return notifications.map(notification => this.mapToNotificationModel(notification));
      })
      .catch(error => {
        console.error('❌ Erreur chargement notifications:', error);
        return [];
      });
  }

  /**
   * Marque une notification comme lue
   */
  markAsRead(notificationId: string): Promise<void> {
    return fetch(`${this.baseUrl}/api/notifications/${notificationId}/read`, {
      method: 'POST'
    })
      .then(response => {
        if (!response.ok) {
          throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        console.log('✅ Notification marquée comme lue:', notificationId);
      })
      .catch(error => {
        console.error('❌ Erreur marquer comme lu:', error);
      });
  }

  /**
   * Marque toutes les notifications comme lues
   */
  markAllAsRead(userId: string): Promise<void> {
    return fetch(`${this.baseUrl}/api/notifications/user/${userId}/mark-all-read`, {
      method: 'POST'
    })
      .then(response => {
        if (!response.ok) {
          throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        console.log('✅ Toutes les notifications marquées comme lues');
      })
      .catch(error => {
        console.error('❌ Erreur marquer tout comme lu:', error);
      });
  }

  /**
   * Supprime une notification
   */
  deleteNotification(notificationId: String): Promise<void> {
    return fetch(`${this.baseUrl}/api/notifications/${notificationId}`, {
      method: 'DELETE'
    })
      .then(response => {
        if (!response.ok) {
          throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        console.log('✅ Notification supprimée:', notificationId);
      })
      .catch(error => {
        console.error('❌ Erreur suppression notification:', error);
      });
  }

}
