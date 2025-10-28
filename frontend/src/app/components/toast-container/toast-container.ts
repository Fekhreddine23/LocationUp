import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Toast } from '../../core/models/toast.model';
import { NotificationService } from '../../core/services/notification.service';
import { ToastComponent } from '../toast/toast';

@Component({
  selector: 'app-toast-container',
  standalone: true,
  imports: [CommonModule, ToastComponent],
  templateUrl: './toast-container.html',
  styleUrl: './toast-container.scss'
})
export class ToastContainer implements OnInit {
   

  toasts: Toast[] = [];

  constructor(private notificationService: NotificationService) {}

  ngOnInit(): void {
    this.notificationService.toasts$.subscribe(toasts => {
      this.toasts = toasts;
    });
  }

  removeToast(id: number): void {
    this.notificationService.remove(id);
  }


}
