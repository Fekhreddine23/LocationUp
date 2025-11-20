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
import { BaseChartDirective, provideCharts, withDefaultRegisterables } from 'ng2-charts';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';
import { DashboardTrends } from '../../../core/models/DashboardTrends.model';

@Component({
  selector: 'app-admin-dashboards',
  standalone: true,
  imports: [CommonModule, RouterModule, Breadcrumbs, Spinner, HeathStatus, BaseChartDirective],
  providers: [provideCharts(withDefaultRegisterables())],
  templateUrl: './admin-dashboards.html',
  styleUrls: ['./admin-dashboards.scss']
})
export class AdminDashboards implements OnInit, OnDestroy {

  isLoading = false;
  errorMessage = '';
  trendsLoading = false;

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
  dashboardTrends: DashboardTrends | null = null;
  trendRanges = [3, 6, 12];
  selectedTrendRange = 6;

  breadcrumbItems = [
    { label: 'Administration', url: '/admin', active: true }
  ];

  private destroy$ = new Subject<void>();

  constructor(
    private adminService: AdminService,
    private router: Router,
    private healthService: HealthStatusService
  ) { }

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
        this.adminStats = stats;
        this.updateCharts(); // ← AJOUT CHARTS.JS
        this.loadRecentUsers();
        this.loadDashboardTrends();
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



  //============== CHART.JS ==============

  // Charts Configuration
  public barChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    plugins: {
      legend: { display: true },
      title: { display: true, text: 'Statistiques des Réservations' }
    }
  };

  public barChartLabels: string[] = ['Confirmées', 'En attente', 'Annulées', 'Terminées'];
  public barChartType: ChartType = 'bar';
  public barChartData: ChartData<'bar'> = {
    labels: this.barChartLabels,
    datasets: [
      {
        data: [0, 0, 0, 0],
        label: 'Réservations',
        backgroundColor: ['#28a745', '#ffc107', '#dc3545', '#17a2b8']
      }
    ]
  };

  public pieChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    plugins: {
      legend: { display: true, position: 'top' },
      title: { display: true, text: 'Répartition des Utilisateurs' }
    }
  };

  public pieChartData: ChartData<'pie', number[]> = {
    labels: ['Actifs', 'Inactifs'],
    datasets: [{
      data: [0, 0],
      backgroundColor: ['#28a745', '#6c757d']
    }]
  };


  public pieChartType: ChartType = 'pie';

  public trendsLineOptions: ChartConfiguration['options'] = {
    responsive: true,
    plugins: {
      legend: { display: true },
      title: { display: true, text: 'Évolution des réservations' }
    },
    scales: {
      y: {
        beginAtZero: true,
        position: 'left'
      },
      y1: {
        beginAtZero: true,
        position: 'right'
      }
    }
  };

  public trendsLineData: ChartData<'line'> = {
    labels: [],
    datasets: [
      {
        data: [],
        label: 'Réservations',
        borderColor: '#2563eb',
        backgroundColor: 'rgba(37, 99, 235, 0.2)',
        fill: true,
        tension: 0.3
      },
      {
        data: [],
        label: 'Revenu (€)',
        borderColor: '#16a34a',
        backgroundColor: 'rgba(22, 163, 74, 0.2)',
        fill: true,
        yAxisID: 'y1'
      }
    ]
  };

  public categoryChartData: ChartData<'doughnut', number[]> = {
    labels: [],
    datasets: [
      {
        data: [],
        backgroundColor: ['#2563eb', '#7c3aed', '#f97316', '#0ea5e9', '#22c55e', '#facc15']
      }
    ]
  };

  public cityChartData: ChartData<'bar'> = {
    labels: [],
    datasets: [
      {
        data: [],
        label: 'Offres par ville',
        backgroundColor: '#7c3aed'
      }
    ]
  };


  // Mettre à jour les charts avec les données
  private updateCharts(): void {
    // Graphique réservations
    this.barChartData.datasets[0].data = [
      this.adminStats.totalReservations - this.adminStats.pendingReservations, // Confirmées
      this.adminStats.pendingReservations, // En attente
      0, // Annulées (à récupérer si disponible)
      0  // Terminées (à récupérer si disponible)
    ];

    // Graphique utilisateurs
    this.pieChartData.datasets[0].data = [
      this.adminStats.activeUsers,
      this.adminStats.totalUsers - this.adminStats.activeUsers
    ];
  }

  loadDashboardTrends(): void {
    this.trendsLoading = true;
    this.adminService.getDashboardTrends(this.selectedTrendRange).subscribe({
      next: (trends) => {
        this.dashboardTrends = trends;
        this.updateTrendCharts();
        this.trendsLoading = false;
      },
      error: () => {
        this.trendsLoading = false;
      }
    });
  }

  private updateTrendCharts(): void {
    if (!this.dashboardTrends) {
      return;
    }
    const months = this.dashboardTrends.reservationsByMonth.map((item) => item.month);
    this.trendsLineData.labels = months;
    this.trendsLineData.datasets[0].data = this.dashboardTrends.reservationsByMonth.map((item) => item.reservations);
    this.trendsLineData.datasets[1].data = this.dashboardTrends.reservationsByMonth.map((item) => item.revenue);

    this.categoryChartData.labels = this.dashboardTrends.offersByCategory.map((item) => item.category);
    this.categoryChartData.datasets[0].data = this.dashboardTrends.offersByCategory.map((item) => item.count);

    this.cityChartData.labels = this.dashboardTrends.topPickupCities.map((item) => item.city);
    this.cityChartData.datasets[0].data = this.dashboardTrends.topPickupCities.map((item) => item.count);
  }

  changeTrendRange(range: number): void {
    if (this.selectedTrendRange === range) {
      return;
    }
    this.selectedTrendRange = range;
    this.loadDashboardTrends();
  }

  exportTrendsCSV(): void {
    if (!this.dashboardTrends) {
      return;
    }
    const lines: string[] = [];
    lines.push('"Mois","Réservations","Revenu (€)"');
    this.dashboardTrends.reservationsByMonth.forEach((stat) => {
      lines.push(`"${stat.month}",${stat.reservations},${stat.revenue}`);
    });
    lines.push('');
    lines.push('"Catégorie","Nombre d\'offres"');
    this.dashboardTrends.offersByCategory.forEach((cat) => {
      lines.push(`"${cat.category}",${cat.count}`);
    });
    lines.push('');
    lines.push('"Ville","Offres pickup"');
    this.dashboardTrends.topPickupCities.forEach((city) => {
      lines.push(`"${city.city}",${city.count}`);
    });

    const blob = new Blob([lines.join('\n')], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `dashboard-trends-${Date.now()}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  }
}
