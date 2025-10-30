import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { Breadcrumbs } from '../breadcrumbs/breadcrumbs';
import { AdminBooking, BookingResponse } from '../../core/models/AdminBooking.model';
import { AdminService } from '../../core/services/admin.service';

type BookingStatus = AdminBooking['status'] | 'ALL';

@Component({
  selector: 'app-booking-management',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, Breadcrumbs],
  templateUrl: './booking-management.html',
  styleUrl: './booking-management.scss'
})
export class BookingManagement implements OnInit {

  bookings: AdminBooking[] = [];
  filteredBookings: AdminBooking[] = [];
  selectedBooking: AdminBooking | null = null;
  
  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;
  
  // Filtres
  searchQuery = '';
  statusFilter: BookingStatus = 'ALL';
  dateFilter = '';
  
  // États
  isLoading = false;
  isDetailModalOpen = false;
  isDeleteModalOpen = false;
  
  // Messages
  successMessage = '';
  errorMessage = '';

  // Statistiques
  bookingStats: any = {};

  breadcrumbItems = [
    { label: 'Administration', url: '/admin' },
    { label: 'Gestion des Réservations', url: '/admin/bookings', active: true }
  ];

  constructor(
    private adminService: AdminService,
    private router: Router
  ) {}

  private static normalizeStatus(status?: string | null): AdminBooking['status'] {
    const allowed: AdminBooking['status'][] = ['PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED'];
    if (!status) {
      return 'PENDING';
    }
    const upper = status.toUpperCase();
    return allowed.includes(upper as AdminBooking['status']) ? (upper as AdminBooking['status']) : 'PENDING';
  }

  ngOnInit(): void {
    this.loadBookings();
    this.loadBookingStats();
  }

  loadBookings(): void {
    this.isLoading = true;
    this.adminService.getAllBookings(this.currentPage, this.pageSize).subscribe({
      next: (response: BookingResponse) => {
        this.bookings = response.content;
        this.filteredBookings = [...this.bookings];
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Erreur chargement réservations:', error);
        this.errorMessage = 'Erreur lors du chargement des réservations';
        this.isLoading = false;
      }
    });
  }

  loadBookingStats(): void {
    this.adminService.getBookingStats().subscribe({
      next: (stats) => {
        this.bookingStats = stats;
      },
      error: (error) => {
        console.error('Erreur chargement stats:', error);
      }
    });
  }

  searchBookings(): void {
    if (this.searchQuery.trim()) {
      this.isLoading = true;
      this.adminService.searchBookings(this.searchQuery, this.currentPage, this.pageSize).subscribe({
        next: (response: BookingResponse) => {
          this.filteredBookings = response.content;
          this.totalElements = response.totalElements;
          this.totalPages = response.totalPages;
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Erreur recherche:', error);
          this.errorMessage = 'Erreur lors de la recherche';
          this.isLoading = false;
        }
      });
    } else {
      this.filteredBookings = [...this.bookings];
    }
  }

  applyFilters(): void {
    let filtered = [...this.bookings];

    // Filtre par statut
    if (this.statusFilter !== 'ALL') {
      filtered = filtered.filter(booking => BookingManagement.normalizeStatus(booking.status) === this.statusFilter);
    }

    // Filtre par date
    if (this.dateFilter) {
      filtered = filtered.filter(booking => 
        booking.reservationDate.includes(this.dateFilter)
      );
    }

    this.filteredBookings = filtered;
  }

  // Actions sur les réservations
  viewBookingDetails(booking: AdminBooking): void {
    this.selectedBooking = booking;
    this.isDetailModalOpen = true;
  }

  confirmBooking(booking: AdminBooking): void {
    if (!booking.reservationId) {
      return;
    }
    this.adminService.updateBookingStatus(booking.reservationId, 'CONFIRMED').subscribe({
      next: (updatedBooking) => {
        booking.status = BookingManagement.normalizeStatus(updatedBooking.status);
        this.successMessage = 'Réservation confirmée avec succès';
        this.clearMessagesAfterDelay();
        this.loadBookingStats(); // Recharger les stats
      },
      error: (error) => {
        console.error('Erreur confirmation:', error);
        this.errorMessage = 'Erreur lors de la confirmation';
        this.clearMessagesAfterDelay();
      }
    });
  }

  cancelBooking(booking: AdminBooking): void {
    if (!booking.reservationId) {
      return;
    }
    this.adminService.cancelBooking(booking.reservationId).subscribe({
      next: (updatedBooking) => {
        booking.status = BookingManagement.normalizeStatus(updatedBooking.status);
        this.successMessage = 'Réservation annulée avec succès';
        this.clearMessagesAfterDelay();
        this.loadBookingStats(); // Recharger les stats
      },
      error: (error) => {
        console.error('Erreur annulation:', error);
        this.errorMessage = 'Erreur lors de l\'annulation';
        this.clearMessagesAfterDelay();
      }
    });
  }

  completeBooking(booking: AdminBooking): void {
    if (!booking.reservationId) {
      return;
    }
    this.adminService.updateBookingStatus(booking.reservationId, 'COMPLETED').subscribe({
      next: (updatedBooking) => {
        booking.status = BookingManagement.normalizeStatus(updatedBooking.status);
        this.successMessage = 'Réservation marquée comme terminée';
        this.clearMessagesAfterDelay();
        this.loadBookingStats(); // Recharger les stats
      },
      error: (error) => {
        console.error('Erreur completion:', error);
        this.errorMessage = 'Erreur lors du marquage comme terminée';
        this.clearMessagesAfterDelay();
      }
    });
  }

  confirmDelete(booking: AdminBooking): void {
    this.selectedBooking = booking;
    this.isDeleteModalOpen = true;
  }

  deleteBooking(): void {
    if (!this.selectedBooking) return;
    const reservationId = this.selectedBooking.reservationId;
    if (!reservationId) {
      return;
    }

    this.adminService.deleteBooking(reservationId).subscribe({
      next: () => {
        this.bookings = this.bookings.filter(b => b.reservationId !== reservationId);
        this.filteredBookings = this.filteredBookings.filter(b => b.reservationId !== reservationId);
        this.successMessage = 'Réservation supprimée avec succès';
        this.isDeleteModalOpen = false;
        this.clearMessagesAfterDelay();
        this.loadBookingStats(); // Recharger les stats
      },
      error: (error) => {
        console.error('Erreur suppression:', error);
        this.errorMessage = 'Erreur lors de la suppression';
        this.clearMessagesAfterDelay();
      }
    });
  }

  // Utilitaires
  getStatusText(status?: string): string {
    const statusMap: { [key: string]: string } = {
      'PENDING': 'En attente',
      'CONFIRMED': 'Confirmée',
      'CANCELLED': 'Annulée',
      'COMPLETED': 'Terminée'
    };
    const key = BookingManagement.normalizeStatus(status);
    return statusMap[key] || key;
  }

  getStatusClass(status?: string): string {
    const classMap: { [key: string]: string } = {
      'PENDING': 'status-pending',
      'CONFIRMED': 'status-confirmed',
      'CANCELLED': 'status-cancelled',
      'COMPLETED': 'status-completed'
    };
    const key = BookingManagement.normalizeStatus(status);
    return classMap[key] || 'status-pending';
  }

  formatCurrency(amount?: number): string {
    if (amount == null) {
      return '—';
    }
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: 'EUR'
    }).format(amount);
  }

  formatDate(dateString?: string): string {
    if (!dateString) {
      return '—';
    }
    return new Date(dateString).toLocaleDateString('fr-FR', {
      day: 'numeric',
      month: 'long',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  formatShortDate(dateString?: string): string {
    if (!dateString) {
      return '—';
    }
    return new Date(dateString).toLocaleDateString('fr-FR');
  }

  canConfirm(booking: AdminBooking): boolean {
    return BookingManagement.normalizeStatus(booking.status) === 'PENDING';
  }

  canCancel(booking: AdminBooking): boolean {
    const status = BookingManagement.normalizeStatus(booking.status);
    return status === 'PENDING' || status === 'CONFIRMED';
  }

  canComplete(booking: AdminBooking): boolean {
    return BookingManagement.normalizeStatus(booking.status) === 'CONFIRMED';
  }

  clearMessagesAfterDelay(): void {
    setTimeout(() => {
      this.successMessage = '';
      this.errorMessage = '';
    }, 5000);
  }

  // Pagination
  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadBookings();
    }
  }

  // Navigation
  goBackToDashboard(): void {
    this.router.navigate(['/admin']);
  }

  viewUserBookings(userId?: number): void {
    if (!userId) {
      return;
    }
    this.router.navigate(['/admin/users'], {
      queryParams: { view: 'bookings', userId }
    });
  }

}
