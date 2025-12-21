import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { ChartConfiguration, ChartOptions } from 'chart.js';
import { BaseChartDirective, provideCharts, withDefaultRegisterables } from 'ng2-charts';
import { Breadcrumbs } from '../../../components/breadcrumbs/breadcrumbs';
import { AdminService } from '../../../core/services/admin.service';
import { FinanceAlertFilters, FinanceOverview, OutstandingPoint, PaymentAlert, PaymentEventLogEntry } from '../../../core/models/admin-finance.model';
import { AuthService } from '../../../core/services/auth.service';
import { Observable, map } from 'rxjs';

@Component({
  selector: 'app-admin-finance',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, Breadcrumbs, BaseChartDirective],
  providers: [provideCharts(withDefaultRegisterables())],
  templateUrl: './admin-finance.html',
  styleUrls: ['./admin-finance.scss']
})
export class AdminFinanceComponent implements OnInit {

  overview: FinanceOverview | null = null;
  alerts: PaymentAlert[] = [];
  events: PaymentEventLogEntry[] = [];
  isLoading = false;
  eventsLoading = false;
  alertsLoading = false;
  highlightedAlerts: PaymentAlert[] = [];
  selectedAlert: PaymentAlert | null = null;
  alertEvents: PaymentEventLogEntry[] = [];
  alertEventsLoading = false;
  alertEventsError = '';
  selectedRange = 6;
  alertSeverityFilter: 'ALL' | 'ALERTE' | 'CRITIQUE' = 'ALL';
  alertSearch = '';
  alertActionOnly = false;
  alertDateStart: string | null = null;
  alertDateEnd: string | null = null;
  alertStatusOptions: string[] = ['PENDING', 'REQUIRES_ACTION', 'FAILED', 'EXPIRED'];
  alertStatusSelection: Record<string, boolean> = {
    PENDING: true,
    REQUIRES_ACTION: true,
    FAILED: true,
    EXPIRED: false
  };
  isAdmin$!: Observable<boolean>;

  breadcrumbItems = [
    { label: 'Administration', url: '/admin' },
    { label: 'Finances', url: '/admin/finance', active: true }
  ];

  lineChartData: ChartConfiguration<'line'>['data'] = {
    labels: [],
    datasets: [
      {
        data: [],
        label: 'Revenus (€)',
        fill: false,
        tension: 0.4,
        borderColor: '#2563eb',
        backgroundColor: '#bfdbfe'
      }
    ]
  };

  lineChartOptions: ChartOptions<'line'> = {
    responsive: true,
    plugins: {
      legend: { display: true }
    }
  };

  constructor(
    private adminService: AdminService,
    private authService: AuthService,
    private router: Router
  ) {
    this.isAdmin$ = this.authService.currentUser.pipe(map(user => user?.role === 'ROLE_ADMIN'));
  }

  ngOnInit(): void {
    this.loadOverview();
    this.loadAlerts();
    this.loadEvents();
  }

  loadOverview(): void {
    this.isLoading = true;
    this.adminService.getFinanceOverview(this.selectedRange).subscribe({
      next: (overview) => {
        this.overview = overview;
        this.highlightedAlerts = overview.alerts ?? [];
        this.updateChart();
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  loadAlerts(): void {
    this.alertsLoading = true;
    this.adminService.getFinanceAlerts(this.buildAlertFilters()).subscribe({
      next: (alerts) => {
        this.alerts = alerts;
        this.alertsLoading = false;
      },
      error: () => {
        this.alertsLoading = false;
      }
    });
  }

  loadEvents(): void {
    this.eventsLoading = true;
    this.adminService.getFinanceEvents(20).subscribe({
      next: (events) => {
        this.events = events;
        this.eventsLoading = false;
      },
      error: () => {
        this.eventsLoading = false;
      }
    });
  }

  refreshRange(range: number): void {
    this.selectedRange = range;
    this.loadOverview();
  }

  downloadCsv(): void {
    this.adminService.exportFinanceCsv(this.selectedRange, 'overview').subscribe(blob => {
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = 'finance-report.csv';
      link.click();
      window.URL.revokeObjectURL(url);
    });
  }

  downloadAlertsCsv(): void {
    this.adminService.exportFinanceCsv(this.selectedRange, 'alerts', this.buildAlertFilters()).subscribe(blob => {
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = 'finance-alerts.csv';
      link.click();
      window.URL.revokeObjectURL(url);
    });
  }

  private updateChart(): void {
    if (!this.overview) {
      return;
    }
    this.lineChartData.labels = this.overview.revenueHistory.map(point => point.label);
    this.lineChartData.datasets[0].data = this.overview.revenueHistory.map(point => point.revenue);
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'PAID':
        return 'status-paid';
      case 'FAILED':
        return 'status-failed';
      case 'REQUIRES_ACTION':
        return 'status-action';
      default:
        return 'status-pending';
    }
  }

  isActionRequiredStatus(status: string): boolean {
    return ['PENDING', 'REQUIRES_ACTION', 'FAILED', 'EXPIRED'].includes(status?.toUpperCase());
  }

  setAlertSeverity(filter: 'ALL' | 'ALERTE' | 'CRITIQUE'): void {
    this.alertSeverityFilter = filter;
    this.applyAlertFilters();
  }

  toggleActionRequired(): void {
    this.alertActionOnly = !this.alertActionOnly;
    this.applyAlertFilters();
  }

  onStatusToggle(): void {
    this.applyAlertFilters();
  }

  applyAlertFilters(): void {
    this.loadAlerts();
  }

  resetAlertFilters(): void {
    this.alertSeverityFilter = 'ALL';
    this.alertSearch = '';
    this.alertActionOnly = false;
    this.alertDateStart = null;
    this.alertDateEnd = null;
    this.alertStatusSelection = {
      PENDING: true,
      REQUIRES_ACTION: true,
      FAILED: true,
      EXPIRED: false
    };
    this.loadAlerts();
  }

  get filteredAlerts(): PaymentAlert[] {
    return this.alerts;
  }

  // ajoute des helpers formatWeekLabel/formatMonthLabel 
  // pour formater les périodes issues des agrégats hebdo/mensuels.
  formatWeekLabel(point: OutstandingPoint): string {
    if (!point?.period) {
      return '—';
    }
    const [year, week] = point.period.split('-');
    return `S${week ?? ''} ${year ?? ''}`.trim();
  }

  formatMonthLabel(point: OutstandingPoint): string {
    if (!point?.period) {
      return '—';
    }
    const [year, month] = point.period.split('-');
    return month && year ? `${month}/${year}` : point.period;
  }

  private buildAlertFilters(): FinanceAlertFilters {
    const statuses = this.alertStatusOptions.filter(status => this.alertStatusSelection[status]);
    return {
      severity: this.alertSeverityFilter !== 'ALL' ? this.alertSeverityFilter : undefined,
      statuses,
      search: this.alertSearch?.trim() || undefined,
      startDate: this.alertDateStart || undefined,
      endDate: this.alertDateEnd || undefined,
      actionRequiredOnly: this.alertActionOnly || undefined
    };
  }

  viewAlertDetails(alert: PaymentAlert): void {
    if (!alert?.reservationId) {
      return;
    }
    this.selectedAlert = alert;
    this.alertEvents = [];
    this.alertEventsError = '';
    this.alertEventsLoading = true;
    this.adminService.getBookingPaymentEvents(alert.reservationId).subscribe({
      next: (events) => {
        this.alertEvents = events ?? [];
        this.alertEventsLoading = false;
      },
      error: (error) => {
        console.error('Erreur récupération événements de paiement', error);
        this.alertEventsError = 'Impossible de charger l’historique Stripe pour cette réservation.';
        this.alertEventsLoading = false;
      }
    });
  }

  closeAlertDetails(): void {
    this.selectedAlert = null;
    this.alertEvents = [];
    this.alertEventsError = '';
    this.alertEventsLoading = false;
  }

  navigateToBooking(alert: PaymentAlert): void {
    if (!alert?.reservationId) {
      return;
    }
    this.router.navigate(['/admin/bookings'], {
      queryParams: { reservationId: alert.reservationId }
    }).catch(err => console.warn('Navigation booking management impossible', err));
  }

  get awaitingIdentityCount(): number {
    if (!this.overview) {
      return 0;
    }
    return (this.overview.identitiesPending ?? 0) + (this.overview.identitiesRequiresInput ?? 0);
  }

  navigateToIdentityMonitoring(): void {
    this.router.navigate(['/admin/bookings'], {
      queryParams: { identity: 'UNVERIFIED' }
    }).catch(err => console.warn('Navigation filtres identité impossible', err));
  }

  trackByReservation(_: number, alert: PaymentAlert): number | string {
    return alert.reservationId ?? alert.message;
  }
}
