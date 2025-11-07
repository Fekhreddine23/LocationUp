import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';

import { HealthStatusService } from '../../core/services/health/healthStatusService';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-heath-status',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './heath-status.html',
  styleUrl: './heath-status.scss'
})
export class HeathStatus implements OnInit, OnDestroy {

  isHealthy = false;
  lastCheck = '';
  isAdmin = false;

  private authSubscription?: Subscription;
  private healthCheckIntervalId?: ReturnType<typeof setInterval>;

  constructor(
    private readonly healthService: HealthStatusService,
    private readonly authService: AuthService
  ) {}

  ngOnInit(): void {
    this.authSubscription = this.authService.currentUser.subscribe(user => {
      const isAdminUser = user?.role === 'ROLE_ADMIN';
      if (isAdminUser !== this.isAdmin) {
        this.isAdmin = !!isAdminUser;
        this.isAdmin ? this.startHealthChecks() : this.stopHealthChecks();
      } else if (this.isAdmin && !this.healthCheckIntervalId) {
        this.startHealthChecks();
      }
    });
  }

  ngOnDestroy(): void {
    this.stopHealthChecks();
    this.authSubscription?.unsubscribe();
  }

  private startHealthChecks(): void {
    this.stopHealthChecks();
    this.checkHealth();
    this.healthCheckIntervalId = setInterval(() => this.checkHealth(), 60000);
  }

  private stopHealthChecks(): void {
    if (this.healthCheckIntervalId) {
      clearInterval(this.healthCheckIntervalId);
      this.healthCheckIntervalId = undefined;
    }
  }

  checkHealth(): void {
    if (!this.isAdmin) {
      return;
    }

    this.healthService.checkLiveness().subscribe((healthy: boolean) => {
      this.isHealthy = healthy;
      this.lastCheck = new Date().toLocaleTimeString();
    });
  }

}
