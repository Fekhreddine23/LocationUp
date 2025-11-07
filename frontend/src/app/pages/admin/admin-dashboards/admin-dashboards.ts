import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { Subject, timer } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AdminStats } from '../../../core/models/AdminStats.model';
import { AdminUser } from '../../../core/models/AdminUser.model';
import { RecentActivity } from '../../../core/models/RecentActivity.model';
import { AdminService } from '../../../core/services/admin.service';
import { UserResponse } from '../../../core/models/UserResponse.model';
import { Breadcrumbs } from '../../../components/breadcrumbs/breadcrumbs';
import { Spinner } from '../../../components/spinner/spinner';
import { HealthStatusService } from '../../../core/services/health/healthStatusService';
import { HeathStatus } from '../../../components/heath-status/heath-status';

@Component({
  selector: 'app-admin-dashboards',
  standalone: true,
  imports: [CommonModule, RouterModule, Breadcrumbs, Spinner, HeathStatus],
  templateUrl: './admin-dashboards.html',
  styleUrls: ['./admin-dashboards.scss']
})
export class AdminDashboards implements OnInit, OnDestroy {
  
  isLoading = false;
  errorMessage = '';

    systemHealth: any; //variable pour stocker le statut de santé du système

  
  adminStats: AdminStats = {
    totalUsers: 0,
    activeUsers: 0,
    totalReservations: 0,
    pendingReservations: 0,
    totalOffers: 0,
    totalRevenue: 0
  };
  
  recentUsers: AdminUser[] = [];
  recentActivity: RecentActivity[] = [];
  
  breadcrumbItems = [
    { label: 'Administration', url: '/admin', active: true }
  ];

  private destroy$ = new Subject<void>();

  constructor(
    private adminService: AdminService,
    private router: Router,
    private healthService: HealthStatusService 
  ) {}

  ngOnInit(): void {
    this.loadAdminData();
    this.setupAutoRefresh();
    this.loadSystemHealth();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  setupAutoRefresh(): void {
    // Refresh automatique toutes les 30 secondes
    timer(30000, 30000)
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        if (!this.isLoading) {
          this.loadAdminData();
        }
      });
  }

  loadAdminData(): void {
    this.isLoading = true;
    this.errorMessage = '';

    // Charger les statistiques admin
    this.adminService.getAdminStats().subscribe({
      next: (stats) => {
        this.adminStats = stats;
        this.loadRecentUsers();
      },
      error: (error) => {
        console.error('Erreur chargement stats admin:', error);
        this.errorMessage = 'Erreur lors du chargement des données admin';
        this.isLoading = false;
      }
    });
  }

  loadRecentUsers(): void {
    this.adminService.getAllUsers(0, 5).subscribe({
      next: (response: UserResponse) => {
        this.recentUsers = response.content;
        this.loadRecentActivity();
      },
      error: (error) => {
        console.error('Erreur chargement utilisateurs:', error);
        this.isLoading = false;
      }
    });
  }

  loadRecentActivity(): void {
    this.adminService.getRecentActivity().subscribe({
      next: (activity) => {
        this.recentActivity = activity;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Erreur chargement activité:', error);
        this.isLoading = false;
      }
    });
  }

  // ==================== MÉTHODES UTILITAIRES ====================

  formatNumber(num: number): string {
    return num.toLocaleString('fr-FR');
  }

  formatCurrency(amount: number): string {
    return this.adminService.formatCurrency(amount);
  }

  getRoleText(role: string): string {
    const roles: { [key: string]: string } = {
      'ROLE_ADMIN': 'Administrateur',
      'ROLE_USER': 'Utilisateur',
      'ROLE_MODERATOR': 'Modérateur'
    };
    return roles[role] || role;
  }

  getRoleClass(role: string): string {
    return role === 'ROLE_ADMIN' ? 'role-admin' : 'role-user';
  }

  getActivityIcon(type: string): string {
    const icons: Record<string, string> = {
      'RESERVATION': '📋',
      'USER': '👤',
      'OFFER': '🚗'
    };
    return icons[type] || 'ℹ️';
  }

  getUserDisplayName(user: AdminUser): string {
    if (user.firstName && user.lastName) {
      return `${user.firstName} ${user.lastName}`;
    }
    return user.username;
  }

  getUserInitials(user: AdminUser): string {
    if (user.firstName && user.lastName) {
      return `${user.firstName.charAt(0)}${user.lastName.charAt(0)}`.toUpperCase();
    }
    return user.username.charAt(0).toUpperCase();
  }

  formatDate(dateString?: string): string {
    if (!dateString) {
      return '';
    }
    return this.adminService.formatDate(dateString);
  }

  getActivityDescription(activity: RecentActivity): string {
    const userPart = activity.user ? `${activity.user.username} - ` : '';
    return `${userPart}${activity.description}`;
  }

  getTimeAgo(timestamp?: string): string {
    if (!timestamp) {
      return '';
    }
    const date = new Date(timestamp);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    
    if (diffMins < 1) return 'À l\'instant';
    if (diffMins < 60) return `Il y a ${diffMins} min`;
    if (diffHours < 24) return `Il y a ${diffHours} h`;
    
    return this.formatDate(timestamp);
  }

  // ==================== NAVIGATION ====================

  goToUserManagement(): void {
    this.router.navigate(['/admin/users']);
  }

  goToBookingManagement(): void {
    this.router.navigate(['/admin/bookings']);
  }

  goToOfferManagement(): void {
    this.router.navigate(['/admin/offers']);
  }

  refreshData(): void {
    this.loadAdminData();
  }



  //===================== SANTÉ DU SYSTÈME ====================
  loadSystemHealth() {
    this.healthService.checkBackendHealth().subscribe({
      next: (health) => {
        this.systemHealth = health;
      },
      error: (error) => {
        console.error('❌ Erreur health check:', error);
      }
    });
  }
}
