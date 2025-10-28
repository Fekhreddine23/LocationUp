import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-confirmation-modal',
  standalone: true,
  imports: [CommonModule, DatePipe, FormsModule],
  templateUrl: './confirmation-modal.html',
  styleUrl: './confirmation-modal.scss'
})
export class ConfirmationModal {

  @Input() isVisible = false;
  @Input() bookingStatus: string = '';
  @Input() bookingDetails: any = null;
  @Input() showReasonInput = false;
  @Input() isConfirming = false;
  
  @Output() confirmed = new EventEmitter<{reason?: string}>();
  @Output() cancelled = new EventEmitter<void>();

  selectedReason = '';
  customReason = '';

  getMessage(): string {
    const messages: { [key: string]: string } = {
      'PENDING': 'Êtes-vous sûr de vouloir annuler cette réservation en attente ?',
      'CONFIRMED': '⚠️ Attention ! Vous êtes sur le point d\'annuler une réservation confirmée. Cette action est irréversible.',
      'CANCELLED': 'Cette réservation est déjà annulée.',
      'COMPLETED': 'Cette réservation est déjà terminée.'
    };

    return messages[this.bookingStatus] || 'Êtes-vous sûr de vouloir annuler cette réservation ?';
  }

  onConfirm(): void {
    const reason = this.selectedReason === 'other' 
      ? this.customReason 
      : this.selectedReason;

    this.confirmed.emit({ reason: reason || undefined });
  }

  onCancel(): void {
    this.resetForm();
    this.cancelled.emit();
  }

  onOverlayClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('modal-overlay')) {
      this.onCancel();
    }
  }

  private resetForm(): void {
    this.selectedReason = '';
    this.customReason = '';
  }

}
