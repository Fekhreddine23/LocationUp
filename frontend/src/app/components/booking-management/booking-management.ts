import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Breadcrumbs } from '../breadcrumbs/breadcrumbs';
import { AdminBooking, BookingResponse } from '../../core/models/AdminBooking.model';
import { AdminService } from '../../core/services/admin.service';
import { AdminUser } from '../../core/models/AdminUser.model';
import { Offer } from '../../core/models/offer.model';
import { Subject, forkJoin, Observable, of } from 'rxjs';
import { catchError, map, takeUntil, tap } from 'rxjs/operators';

type BookingStatus = AdminBooking['status'] | 'ALL';
type NormalizedBookingOffer = NonNullable<AdminBooking['offer']>;

@Component({
  selector: 'app-booking-management',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, Breadcrumbs],
  templateUrl: './booking-management.html',
  styleUrl: './booking-management.scss'
})
export class BookingManagement implements OnInit, OnDestroy {

  bookings: AdminBooking[] = [];
  filteredBookings: AdminBooking[] = [];
  displayedBookingsCount = 0;
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
  
  // Filtre utilisateur actif
  private activeUserFilter: { id: number, name: string } | null = null;
  private readonly destroy$ = new Subject<void>();
  private userCache = new Map<number, AdminUser>();
  private offerCache = new Map<number, Offer>();

  breadcrumbItems = [
    { label: 'Administration', url: '/admin' },
    { label: 'Gestion des Réservations', url: '/admin/bookings', active: true }
  ];

  constructor(
    private adminService: AdminService,
    private router: Router,
    private route: ActivatedRoute
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
    this.checkUrlFilter();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadBookings(): void {
    this.isLoading = true;
    this.adminService.getAllBookings(this.currentPage, this.pageSize).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (response: BookingResponse) => {
        this.bookings = response.content ?? [];
        this.totalElements = response.totalElements ?? this.bookings.length;
        this.totalPages = response.totalPages ?? Math.max(1, Math.ceil(this.totalElements / this.pageSize));
        this.hydrateBookings(this.bookings);
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
      this.adminService.searchBookings(this.searchQuery, this.currentPage, this.pageSize).pipe(
        takeUntil(this.destroy$)
      ).subscribe({
        next: (response: BookingResponse) => {
          this.bookings = response.content ?? [];
          this.totalElements = response.totalElements ?? this.bookings.length;
          this.totalPages = response.totalPages ?? Math.max(1, Math.ceil(this.totalElements / this.pageSize));
          this.hydrateBookings(this.bookings);
        },
        error: (error) => {
          console.error('Erreur recherche:', error);
          this.errorMessage = 'Erreur lors de la recherche';
          this.isLoading = false;
        }
      });
    } else {
      // Si recherche vide, recharge toutes les réservations
      this.loadBookings();
    }
  }

  // 🆕 MÉTHODE CORRIGÉE : Applique tous les filtres
  applyFilters(): void {
    let filtered = [...this.bookings];

    // Filtre par utilisateur (si actif)
    if (this.activeUserFilter) {
      filtered = filtered.filter(booking => 
        booking.user?.id === this.activeUserFilter!.id
      );
    }

    // Filtre par statut
    if (this.statusFilter !== 'ALL') {
      filtered = filtered.filter(booking => 
        BookingManagement.normalizeStatus(booking.status) === this.statusFilter
      );
    }

    // Filtre par date
    if (this.dateFilter) {
      filtered = filtered.filter(booking => 
        booking.reservationDate && booking.reservationDate.includes(this.dateFilter)
      );
    }

    this.commitFilteredBookings(filtered);
  }

  private hydrateBookings(bookings: AdminBooking[]): void {
    this.enrichBookingsWithDetails(bookings).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: () => {
        this.applyFilters();
        this.isLoading = false;
      },
      error: () => {
        this.applyFilters();
        this.isLoading = false;
      }
    });
  }

  private enrichBookingsWithDetails(bookings: AdminBooking[]): Observable<void> {
    this.applyCachedUserData(bookings);
    this.applyCachedOfferData(bookings);

    const requests = [
      ...this.buildUserRequests(bookings),
      ...this.buildOfferRequests(bookings)
    ];

    if (!requests.length) {
      return of(void 0);
    }

    return forkJoin(requests).pipe(map(() => void 0));
  }

  private buildUserRequests(bookings: AdminBooking[]): Observable<AdminUser | null>[] {
    const idsToFetch = Array.from(new Set(
      bookings
        .map(booking => booking.user?.id)
        .filter((id): id is number => typeof id === 'number')
    )).filter(id => this.shouldFetchUserDetails(id));

    return idsToFetch.map(id =>
      this.adminService.getUserById(id).pipe(
        tap(user => {
          if (user) {
            this.userCache.set(id, user);
            this.applyUserToBookings(user);
          }
        }),
        catchError(error => {
          console.error('Erreur chargement utilisateur:', error);
          return of(null);
        })
      )
    );
  }

  private buildOfferRequests(bookings: AdminBooking[]): Observable<Offer | null>[] {
    const idsToFetch = Array.from(new Set(
      bookings
        .map(booking => booking.offer?.offerId)
        .filter((id): id is number => typeof id === 'number')
    )).filter(id => this.shouldFetchOfferDetails(id));

    return idsToFetch.map(id =>
      this.adminService.getOfferById(id).pipe(
        tap(offer => {
          if (offer) {
            this.offerCache.set(id, offer);
            this.applyOfferToBookings(offer);
          }
        }),
        catchError(error => {
          console.error('Erreur chargement offre:', error);
          return of(null);
        })
      )
    );
  }

  private applyCachedUserData(bookings: AdminBooking[]): void {
    bookings.forEach(booking => {
      const userId = booking.user?.id;
      if (!userId) {
        return;
      }
      const cached = this.userCache.get(userId);
      if (cached) {
        booking.user = {
          id: cached.id,
          username: cached.username,
          email: cached.email
        };
      }
    });
  }

  private applyCachedOfferData(bookings: AdminBooking[]): void {
    bookings.forEach(booking => {
      const offerId = booking.offer?.offerId;
      if (!offerId) {
        return;
      }
      const cached = this.offerCache.get(offerId);
      if (cached) {
        const mappedOffer = this.mapOfferToBookingOffer(cached);
        booking.offer = mappedOffer;
        booking.totalPrice = booking.totalPrice ?? mappedOffer.price;
      }
    });
  }

  private shouldFetchUserDetails(userId: number): boolean {
    if (this.userCache.has(userId)) {
      return false;
    }
    const booking = this.bookings.find(item => item.user?.id === userId);
    if (!booking) {
      return false;
    }
    const username = booking.user?.username ?? '';
    const email = booking.user?.email ?? '';
    const placeholderUsername = !username || username.startsWith('Utilisateur #');
    return placeholderUsername || !email;
  }

  private shouldFetchOfferDetails(offerId: number): boolean {
    if (this.offerCache.has(offerId)) {
      return false;
    }
    const booking = this.bookings.find(item => item.offer?.offerId === offerId);
    if (!booking || !booking.offer) {
      return false;
    }
    const service = booking.offer.mobilityService ?? '';
    const hasReadableService = service.length > 0 && !service.startsWith('Service #');
    const hasLocation = !!booking.offer.pickupLocation;
    return !hasReadableService || !hasLocation;
  }

  private applyUserToBookings(user: AdminUser): void {
    this.bookings.forEach(booking => {
      if (booking.user?.id === user.id) {
        booking.user = {
          id: user.id,
          username: user.username,
          email: user.email
        };
      }
    });
  }

  private applyOfferToBookings(offer: Offer): void {
    const mappedOffer = this.mapOfferToBookingOffer(offer);
    this.bookings.forEach(booking => {
      if (booking.offer?.offerId === offer.offerId) {
        booking.offer = mappedOffer;
        booking.totalPrice = booking.totalPrice ?? mappedOffer.price;
      }
    });
  }

  private mapOfferToBookingOffer(offer: Offer): NormalizedBookingOffer {
    return {
      offerId: offer.offerId,
      mobilityService: this.getOfferServiceLabel(offer),
      pickupLocation: offer.pickupLocationName || offer.pickupLocation || offer.pickupLocationCity,
      price: offer.price
    };
  }

  private getOfferServiceLabel(offer: Offer): string {
    if (offer.mobilityService && offer.mobilityService.trim().length > 0) {
      return offer.mobilityService;
    }
    if (offer.mobilityServiceId) {
      return `Service #${offer.mobilityServiceId}`;
    }
    return offer.description?.substring(0, 40) ?? 'Service inconnu';
  }

  // 🆕 MÉTHODE : Applique le filtre utilisateur spécifique
  applyUserFilter(userId: number): void {
    this.commitFilteredBookings(this.bookings.filter(booking =>
      booking.user?.id === userId
    ));
  }

  private commitFilteredBookings(bookings: AdminBooking[]): void {
    this.filteredBookings = bookings;
    this.displayedBookingsCount = bookings.length;
  }

  // 🆕 MÉTHODE : Vérifie les filtres dans l'URL
  checkUrlFilter(): void {
    this.route.queryParams
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
        if (params['userId']) {
          const userId = parseInt(params['userId']);
          const userName = params['userName'] || `Utilisateur #${userId}`;
          this.activeUserFilter = { id: userId, name: userName };
          // Applique le filtre une fois les données chargées
          setTimeout(() => this.applyFilters(), 100);
        } else {
          this.activeUserFilter = null;
        }
      });
  }

  // Actions sur les réservations
  viewBookingDetails(booking: AdminBooking): void {
    this.selectedBooking = booking;
    this.isDetailModalOpen = true;
  }

  confirmBooking(booking: AdminBooking): void {
    if (!booking.reservationId) {
      this.errorMessage = 'ID de réservation manquant';
      this.clearMessagesAfterDelay();
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
      this.errorMessage = 'ID de réservation manquant';
      this.clearMessagesAfterDelay();
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
      this.errorMessage = 'ID de réservation manquant';
      this.clearMessagesAfterDelay();
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
    if (!this.selectedBooking?.reservationId) {
      this.errorMessage = 'ID de réservation manquant';
      this.clearMessagesAfterDelay();
      return;
    }

    this.adminService.deleteBooking(this.selectedBooking.reservationId).subscribe({
      next: () => {
        this.bookings = this.bookings.filter(b => b.reservationId !== this.selectedBooking!.reservationId);
        this.totalElements = Math.max(0, this.totalElements - 1);
        this.applyFilters();
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

  // 🆕 MÉTHODE : Format date pour l'affichage des filtres
  formatDisplayDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('fr-FR', {
      day: 'numeric',
      month: 'long',
      year: 'numeric'
    });
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

  // 🆕 MÉTHODES POUR LES FILTRES ACTIFS
  hasActiveFilters(): boolean {
    return this.activeUserFilter !== null || 
           this.statusFilter !== 'ALL' || 
           this.dateFilter !== '';
  }

  getActiveUserFilter(): { id: number, name: string } | null {
    return this.activeUserFilter;
  }

  hasMultipleFilters(): boolean {
    let count = 0;
    if (this.activeUserFilter) count++;
    if (this.statusFilter !== 'ALL') count++;
    if (this.dateFilter) count++;
    return count > 1;
  }

  // Méthodes pour effacer les filtres
  clearUserFilter(): void {
    this.activeUserFilter = null;
    this.router.navigate(['/admin/bookings']);
    this.applyFilters();
  }

  clearStatusFilter(): void {
    this.statusFilter = 'ALL';
    this.applyFilters();
  }

  clearDateFilter(): void {
    this.dateFilter = '';
    this.applyFilters();
  }

  clearAllFilters(): void {
    this.activeUserFilter = null;
    this.statusFilter = 'ALL';
    this.dateFilter = '';
    this.searchQuery = '';
    this.router.navigate(['/admin/bookings']);
    this.applyFilters();
  }
}
