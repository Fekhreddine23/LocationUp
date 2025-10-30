import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { Breadcrumbs } from "../../components/breadcrumbs/breadcrumbs";
import { Booking, BookingsService } from '../../core/services/bookings';
import { UserStatsService } from '../../core/services/user-stats.service';
import { AuthService } from '../../core/services/auth.service';
import { UserStats } from '../../core/models/UserStats.model';
import { User } from '../../core/models/auth.models';
import { Spinner } from "../../components/spinner/spinner";

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, Breadcrumbs, Spinner],

  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit  {
   

  user: User | null = null;
  isLoading = false;
  errorMessage = '';
  
  // Données du dashboard
  userStats: UserStats = {
    totalBookings: 0,
    activeBookings: 0,
    cancelledBookings: 0,
    completedBookings: 0,
    memberSince: ''
  };
  
  recentBookings: Booking[] = [];
  
  breadcrumbItems = [
    { label: 'Tableau de Bord', url: '/dashboard', active: true }
  ];

  constructor(
    private authService: AuthService,
    private bookingsService: BookingsService,
    private userStatsService: UserStatsService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.user = this.authService.currentUserValue;
    
    if (!this.user) {
      this.errorMessage = 'Utilisateur non connecté';
      this.isLoading = false;
      return;
    }

    // Charger les statistiques
    this.userStatsService.getUserStats(this.user.id).subscribe({
      next: (stats) => {
        this.userStats = stats;
        this.loadRecentBookings();
      },
      error: (error) => {
        console.error('Erreur chargement statistiques:', error);
        this.errorMessage = 'Erreur lors du chargement des données';
        this.isLoading = false;
      }
    });
  }

  loadRecentBookings(): void {
    this.bookingsService.getMyBookings().subscribe({
      next: (bookings) => {
        // Prendre les 5 réservations les plus récentes
        this.recentBookings = bookings
          .sort((a, b) => new Date(b.reservationDate).getTime() - new Date(a.reservationDate).getTime())
          .slice(0, 5);
        
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Erreur chargement réservations:', error);
        this.isLoading = false;
      }
    });
  }

  getStatusText(status: string): string {
    const statusMap: { [key: string]: string } = {
      'PENDING': 'En attente',
      'CONFIRMED': 'Confirmée',
      'CANCELLED': 'Annulée',
      'COMPLETED': 'Terminée'
    };
    return statusMap[status] || status;
  }

  getStatusClass(status: string): string {
    switch (status?.toUpperCase()) {
      case 'CONFIRMED': return 'status-confirmed';
      case 'PENDING': return 'status-pending';
      case 'CANCELLED': return 'status-cancelled';
      case 'COMPLETED': return 'status-completed';
      default: return 'status-default';
    }
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  }

  // Navigation rapide
  goToBookings(): void {
    this.router.navigate(['/bookings']);
  }

  goToOffers(): void {
    this.router.navigate(['/offers']);
  }

  goToProfile(): void {
    this.router.navigate(['/profile']);
  }

  createNewBooking(): void {
    this.router.navigate(['/bookings/new']);
  }


}
