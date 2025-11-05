export type NotificationSeverity = 'SUCCESS' | 'ERROR' | 'WARNING' | 'INFO' | 'URGENT';

export interface Notification {
  id?: number;
  type: NotificationSeverity;
  category: string;
  severity?: NotificationSeverity;
  title?: string;
  metadata?: Record<string, unknown>;
  content: string;
  timestamp: string;
  recipient: string;
  read?: boolean;
  dismissible?: boolean;
  actionUrl?: string;
  actionLabel?: string;
  expiresAt?: string;
}
