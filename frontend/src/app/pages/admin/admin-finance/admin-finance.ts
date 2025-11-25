import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ChartConfiguration, ChartOptions } from 'chart.js';
import { BaseChartDirective, provideCharts, withDefaultRegisterables } from 'ng2-charts';
import { Breadcrumbs } from '../../../components/breadcrumbs/breadcrumbs';
import { AdminService } from '../../../core/services/admin.service';
import { FinanceOverview, PaymentAlert, PaymentEventLogEntry } from '../../../core/models/admin-finance.model';

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
  selectedRange = 6;

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

  constructor(private adminService: AdminService) {}

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
        this.updateChart();
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  loadAlerts(): void {
    this.adminService.getFinanceAlerts().subscribe({
      next: (alerts) => this.alerts = alerts
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
    this.adminService.exportFinanceCsv(this.selectedRange).subscribe(blob => {
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = 'finance-report.csv';
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
}
