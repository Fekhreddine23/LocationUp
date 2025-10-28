import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { Toast as ToastModel } from '../../core/models/toast.model';


@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './toast.html',
  styleUrl: './toast.scss'
})
export class ToastComponent {

  @Input() toast: ToastModel | null = null;
  timestamp: any;

  getToastClass(): string {
    return `toast-${this.toast?.type || 'info'}`;
  }

  getIcon(): string {
    const icons = {
      success: '✅',
      error: '❌',
      warning: '⚠️',
      info: 'ℹ️'
    };
    return icons[this.toast?.type || 'info'];
  }

  getTimeAgo(): string {
    if (!this.toast) return '';
    
    const now = new Date();
    const diff = now.getTime() - this.toast.timestamp.getTime();
    const seconds = Math.floor(diff / 1000);
    
    if (seconds < 60) return 'À l\'instant';
    if (seconds < 3600) return `Il y a ${Math.floor(seconds / 60)} min`;
    return `Il y a ${Math.floor(seconds / 3600)} h`;
  }

  onClick(): void {
    // Optionnel: action au clic sur le toast
  }

  onClose(event: Event): void {
    event.stopPropagation();
    // La suppression sera gérée par le container
  }

}
