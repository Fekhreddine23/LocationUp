import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Breadcrumbs } from '../breadcrumbs/breadcrumbs';
import { AdminBooking, BookingExportFilters, BookingResponse } from '../../core/models/AdminBooking.model';
import { AdminService } from '../../core/services/admin.service';
import { AdminUser } from '../../core/models/AdminUser.model';
import { Offer } from '../../core/models/offer.model';
import { PaymentEvent } from '../../core/models/payment-event.model';
import { ReservationAdminAction } from '../../core/models/reservation-admin-action.model';
import { FinanceAlertFilters, PaymentAlert } from '../../core/models/admin-finance.model';
import { Subject, forkJoin, Observable, of } from 'rxjs';
import { catchError, finalize, map, takeUntil, tap } from 'rxjs/operators';
import { NotificationService } from '../../core/services/notification.service';
import { IdentityDocument } from '../../core/models/identity.model';
import { IdentityVerificationRecord } from '../../core/models/identity-admin.model';

type BookingStatus = AdminBooking['status'] | 'ALL';
type NormalizedBookingOffer = NonNullable<AdminBooking['offer']>;

interface BookingStatsOverview {
  totalBookings: number;
  pendingBookings: number;
  confirmedBookings: number;
  cancelledBookings: number;
  completedBookings: number;
  totalRevenue: number;
  outstandingRevenue: number;
  monthToDateRevenue: number;
  confirmationRate: number;
}

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
  paymentEvents: PaymentEvent[] = [];
  paymentEventsLoading = false;
  paymentEventsError = '';
  adminActions: ReservationAdminAction[] = [];
  adminActionsLoading = false;
  adminActionsError = '';
  
  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;
  
  // Filtres
  searchQuery = '';
  statusFilter: BookingStatus = 'ALL';
  dateFilter = '';
  showAnomaliesOnly = false;
  focusedReservationId: number | null = null;
  private pendingReservationFocus: number | null = null;
  
  // États
  isLoading = false;
  isDetailModalOpen = false;
  isDeleteModalOpen = false;
  paymentActionLoading: Record<number, boolean> = {};
  actionAlerts: PaymentAlert[] = [];
  actionAlertsLoading = false;
  actionAlertError = '';
  actionAlertSeverity: 'ALL' | 'ALERTE' | 'CRITIQUE' = 'ALL';
  actionAlertSearch = '';
  actionAlertStatuses: Record<string, boolean> = {
    PENDING: true,
    REQUIRES_ACTION: true,
    FAILED: true,
    EXPIRED: false
  };
  identityFilter: 'ALL' | 'VERIFIED' | 'UNVERIFIED' = 'ALL';
  private readonly alertExportRange = 6;
  
  // Messages
  successMessage = '';
  errorMessage = '';

  // Statistiques
  bookingStats: BookingStatsOverview = this.createEmptyStats();
  statsLoading = true;
  readonly skeletonRows = Array.from({ length: 6 });
  readonly skeletonColumns = Array.from({ length: 7 });
  readonly statSkeletonCards = Array.from({ length: 8 });
  readonly detailSkeletonLines = Array.from({ length: 3 });
  readonly modalSkeletonLines = Array.from({ length: 5 });
  readonly actionAlertSkeletons = Array.from({ length: 4 });
  detailAuxLoading = false;
  private detailLoadingCountdown = 0;
  isDeleteProcessing = false;
  detailTab: 'overview' | 'documents' = 'overview';
  identityDocuments: IdentityDocument[] = [];
  identityDocumentsLoading = false;
  identityDocumentsError = '';
  identityVerifications: IdentityVerificationRecord[] = [];
  identityVerificationsLoading = false;
  identityVerificationsError = '';
  identitySessionLoading = false;
  
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
    private route: ActivatedRoute,
    private notificationService: NotificationService
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
    this.loadActionAlerts();
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
    this.statsLoading = true;
    this.adminService.getBookingStats().pipe(takeUntil(this.destroy$)).subscribe({
      next: (stats) => {
        this.bookingStats = {
          ...this.createEmptyStats(),
          ...stats
        };
        this.statsLoading = false;
      },
      error: (error) => {
        console.error('Erreur chargement stats:', error);
        this.bookingStats = this.createEmptyStats();
        this.statsLoading = false;
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

    if (this.showAnomaliesOnly) {
      filtered = filtered.filter(booking => this.isBookingAnomaly(booking));
    }

    if (this.identityFilter === 'VERIFIED') {
      filtered = filtered.filter(booking => this.isIdentityVerifiedStatus(booking.identityStatus));
    } else if (this.identityFilter === 'UNVERIFIED') {
      filtered = filtered.filter(booking => !this.isIdentityVerifiedStatus(booking.identityStatus));
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
        this.tryFocusReservation();
      },
      error: () => {
        this.applyFilters();
        this.isLoading = false;
        this.tryFocusReservation();
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

  private tryFocusReservation(): void {
    if (!this.pendingReservationFocus) {
      return;
    }
    const targetId = this.pendingReservationFocus;
    const booking = this.bookings.find(item => item.reservationId === targetId);
    if (!booking) {
      return;
    }
    this.focusedReservationId = targetId;
    this.viewBookingDetails(booking);
    this.pendingReservationFocus = null;
    setTimeout(() => this.scrollToBookingRow(targetId), 150);
  }

  private scrollToBookingRow(reservationId: number): void {
    if (typeof document === 'undefined') {
      return;
    }
    const row = document.getElementById(`booking-row-${reservationId}`);
    if (row) {
      row.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
  }

  // 🆕 MÉTHODE : Vérifie les filtres dans l'URL
  checkUrlFilter(): void {
    this.route.queryParams
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
        const identityParam = (params['identity'] ?? '').toString().toUpperCase();
        if (params['userId']) {
          const userId = parseInt(params['userId']);
          const userName = params['userName'] || `Utilisateur #${userId}`;
          this.activeUserFilter = { id: userId, name: userName };
          // Applique le filtre une fois les données chargées
          setTimeout(() => this.applyFilters(), 100);
        } else {
          this.activeUserFilter = null;
        }
        if (params['reservationId']) {
          const reservationId = Number(params['reservationId']);
          this.pendingReservationFocus = Number.isFinite(reservationId) ? reservationId : null;
          if (!this.isLoading) {
            setTimeout(() => this.tryFocusReservation(), 200);
          }
        } else {
          this.pendingReservationFocus = null;
        }
        if (identityParam === 'VERIFIED' || identityParam === 'UNVERIFIED') {
          this.identityFilter = identityParam as typeof this.identityFilter;
          this.applyFilters();
        }
      });
  }

  // Actions sur les réservations
  viewBookingDetails(booking: AdminBooking): void {
    this.focusedReservationId = booking?.reservationId ?? null;
    this.selectedBooking = booking;
    this.isDetailModalOpen = true;
    this.detailTab = 'overview';
    this.paymentEvents = [];
    this.paymentEventsError = '';
    this.adminActions = [];
    this.adminActionsError = '';
    this.identityDocuments = booking.identityDocuments ?? [];
    this.identityDocumentsError = '';
    this.identityVerifications = [];
    this.identityVerificationsError = '';
    if (booking?.reservationId) {
      this.detailLoadingCountdown = 2;
      this.detailAuxLoading = true;
      this.fetchPaymentEvents(booking.reservationId);
      this.fetchAdminActions(booking.reservationId);
    } else {
      this.detailLoadingCountdown = 0;
      this.detailAuxLoading = false;
    }
    if (booking?.user?.id) {
      this.loadIdentityDocuments(booking.user.id);
      this.loadIdentityVerifications(booking.user.id);
    } else {
      this.identityDocumentsLoading = false;
      this.identityVerificationsLoading = false;
    }
  }

  confirmBooking(booking: AdminBooking): void {
    if (!booking.reservationId) {
      const message = 'ID de réservation manquant';
      this.errorMessage = message;
      this.notificationService.error(message, 5000);
      this.clearMessagesAfterDelay();
      return;
    }
    
    this.adminService.updateBookingStatus(booking.reservationId, 'CONFIRMED').subscribe({
      next: (updatedBooking) => {
        booking.status = BookingManagement.normalizeStatus(updatedBooking.status);
        const message = 'Réservation confirmée avec succès';
        this.successMessage = message;
        this.notificationService.success(message, 4000);
        this.clearMessagesAfterDelay();
        this.loadBookingStats(); // Recharger les stats
      },
      error: (error) => {
        console.error('Erreur confirmation:', error);
        const message = error?.message || 'Erreur lors de la confirmation';
        this.errorMessage = message;
        this.notificationService.error(message, 5000);
        this.clearMessagesAfterDelay();
      }
    });
  }

  confirmBookingForced(booking: AdminBooking): void {
    if (!booking.reservationId) {
      const message = 'ID de réservation manquant';
      this.errorMessage = message;
      this.notificationService.error(message, 5000);
      this.clearMessagesAfterDelay();
      return;
    }
    if (!this.confirmAdminAction('Confirmer malgré l\'absence de paiement ?')) {
      return;
    }
    this.adminService.updateBookingStatus(booking.reservationId, 'CONFIRMED').subscribe({
      next: (updatedBooking) => {
        booking.status = BookingManagement.normalizeStatus(updatedBooking.status);
        const message = 'Réservation confirmée sans paiement (action forcée)';
        this.successMessage = message;
        this.notificationService.warning(message, 5000);
        this.clearMessagesAfterDelay();
        this.loadBookingStats();
      },
      error: (error) => {
        console.error('Erreur confirmation forcée:', error);
        const message = error?.message || 'Erreur lors de la confirmation forcée';
        this.errorMessage = message;
        this.notificationService.error(message, 5000);
        this.clearMessagesAfterDelay();
      }
    });
  }

  private fetchPaymentEvents(reservationId: number): void {
    this.paymentEventsLoading = true;
    this.adminService.getBookingPaymentEvents(reservationId).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (events) => {
        this.paymentEvents = events || [];
        this.paymentEventsLoading = false;
        this.resolveDetailLoadingSection();
      },
      error: (error) => {
        console.error('Erreur chargement événements de paiement:', error);
        this.paymentEventsError = 'Impossible de charger l’historique Stripe';
        this.paymentEventsLoading = false;
        this.resolveDetailLoadingSection();
      }
    });
  }

  private fetchAdminActions(reservationId: number): void {
    this.adminActionsLoading = true;
    this.adminService.getBookingAdminActions(reservationId).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (actions) => {
        this.adminActions = actions || [];
        this.adminActionsLoading = false;
        this.resolveDetailLoadingSection();
      },
      error: (error) => {
        console.error('Erreur chargement actions admin:', error);
        this.adminActionsError = 'Impossible de charger l’historique admin';
        this.adminActionsLoading = false;
        this.resolveDetailLoadingSection();
      }
    });
  }

  setDetailTab(tab: 'overview' | 'documents'): void {
    this.detailTab = tab;
  }

  refreshIdentityDocuments(): void {
    if (this.selectedBooking?.user?.id) {
      this.loadIdentityDocuments(this.selectedBooking.user.id);
      this.loadIdentityVerifications(this.selectedBooking.user.id);
    }
  }

  private loadIdentityDocuments(userId: number): void {
    this.identityDocumentsLoading = true;
    this.identityDocumentsError = '';
    this.adminService.getIdentityDocuments(userId).subscribe({
      next: (documents) => {
        this.identityDocuments = documents ?? [];
        this.identityDocumentsLoading = false;
      },
      error: (error) => {
        console.error('Erreur chargement documents identité:', error);
        this.identityDocumentsError = 'Impossible de charger les documents client.';
        this.identityDocumentsLoading = false;
      }
    });
  }

  private loadIdentityVerifications(userId: number): void {
    this.identityVerificationsLoading = true;
    this.identityVerificationsError = '';
    this.adminService.getIdentityVerificationHistory(userId).subscribe({
      next: (records) => {
        this.identityVerifications = records ?? [];
        this.identityVerificationsLoading = false;
      },
      error: (error) => {
        console.error('Erreur historique identité:', error);
        this.identityVerificationsError = 'Impossible de charger l’historique des vérifications.';
        this.identityVerificationsLoading = false;
      }
    });
  }

  requestIdentitySession(): void {
    if (!this.selectedBooking?.user?.id) {
      return;
    }
    this.identitySessionLoading = true;
    const returnUrl = typeof window !== 'undefined'
      ? `${window.location.origin}/profile/identity`
      : '/profile/identity';
    this.adminService.startIdentitySessionForUser(this.selectedBooking.user.id, {
      reservationId: this.selectedBooking.reservationId,
      documentType: 'DRIVER_ID',
      returnUrl
    }).subscribe({
      next: () => {
        this.identitySessionLoading = false;
        this.notificationService.success('Lien Stripe Identity envoyé.');
      },
      error: (error) => {
        console.error('Erreur session identité:', error);
        this.identitySessionLoading = false;
        this.notificationService.error('Impossible de demander un nouveau scan.');
      }
    });
  }

  private resolveDetailLoadingSection(): void {
    if (this.detailLoadingCountdown > 0) {
      this.detailLoadingCountdown--;
      if (this.detailLoadingCountdown === 0) {
        this.detailAuxLoading = false;
      }
    }
  }

  closeDetailModal(): void {
    this.isDetailModalOpen = false;
    this.selectedBooking = null;
    this.detailAuxLoading = false;
    this.detailLoadingCountdown = 0;
    this.identityDocuments = [];
    this.identityDocumentsError = '';
    this.identityVerifications = [];
    this.identityVerificationsError = '';
    this.detailTab = 'overview';
    this.identitySessionLoading = false;
    if (!this.pendingReservationFocus) {
      this.focusedReservationId = null;
    }
  }

  canCollectPayment(booking: AdminBooking): boolean {
    const status = BookingManagement.normalizeStatus(booking.status);
    const paymentStatus = booking.paymentStatus?.toUpperCase();
    return status === 'PENDING' && paymentStatus !== 'PAID';
  }

  collectPayment(booking: AdminBooking): void {
    if (!booking.reservationId) {
      const message = 'ID de réservation manquant';
      this.errorMessage = message;
      this.notificationService.error(message, 5000);
      this.clearMessagesAfterDelay();
      return;
    }

    this.adminService.createBookingPaymentSession(booking.reservationId).subscribe({
      next: (response) => {
        if (response?.paymentUrl) {
          window.open(response.paymentUrl, '_blank', 'noreferrer');
          booking.paymentStatus = 'REQUIRES_ACTION';
          const message = 'Lien de paiement généré. Vérifiez l’onglet ouvert pour encaisser.';
          this.successMessage = message;
          this.notificationService.success(message, 4000);
        } else {
          const message = 'URL de paiement introuvable';
          this.errorMessage = message;
          this.notificationService.warning(message, 5000);
        }
        this.clearMessagesAfterDelay();
      },
      error: (error) => {
        console.error('Erreur paiement manuel:', error);
        const message = error?.message || 'Impossible de créer la session de paiement';
        this.errorMessage = message;
        this.notificationService.error(message, 5000);
        this.clearMessagesAfterDelay();
      }
    });
  }

  forcePaymentExpiration(booking: AdminBooking): void {
    if (!booking.reservationId) {
      const message = 'ID de réservation manquant';
      this.errorMessage = message;
      this.notificationService.error(message, 5000);
      this.clearMessagesAfterDelay();
      return;
    }
    if (!this.confirmAdminAction('Confirmer la mise en EXPIRÉ de ce paiement ?')) {
      return;
    }
    const reason = this.collectOptionalReason('Motif (optionnel) pour l\'expiration forcée :');
    const reservationId = booking.reservationId;
    this.paymentActionLoading[reservationId] = true;
    this.adminService.forcePaymentExpiration(reservationId, reason).pipe(
      finalize(() => {
        this.paymentActionLoading[reservationId] = false;
      })
    ).subscribe({
      next: (updatedBooking) => {
        this.applyBookingPaymentUpdate(booking, updatedBooking);
        const message = 'Paiement marqué comme expiré.';
        this.successMessage = message;
        this.notificationService.success(message, 4000);
        this.clearMessagesAfterDelay();
      },
      error: (error) => {
        console.error('Erreur expiration paiement:', error);
        const message = error?.message || 'Impossible de marquer le paiement comme expiré';
        this.errorMessage = message;
        this.notificationService.error(message, 5000);
        this.clearMessagesAfterDelay();
      }
    });
  }

  forcePaymentRefund(booking: AdminBooking): void {
    if (!booking.reservationId) {
      const message = 'ID de réservation manquant';
      this.errorMessage = message;
      this.notificationService.error(message, 5000);
      this.clearMessagesAfterDelay();
      return;
    }
    if (!this.confirmAdminAction('Confirmer le remboursement manuel de ce paiement ?')) {
      return;
    }
    const reason = this.collectOptionalReason('Motif (optionnel) pour ce remboursement :');
    const reservationId = booking.reservationId;
    this.paymentActionLoading[reservationId] = true;
    this.adminService.forcePaymentRefund(reservationId, reason).pipe(
      finalize(() => {
        this.paymentActionLoading[reservationId] = false;
      })
    ).subscribe({
      next: (updatedBooking) => {
        this.applyBookingPaymentUpdate(booking, updatedBooking);
        const message = 'Paiement remboursé manuellement.';
        this.successMessage = message;
        this.notificationService.success(message, 4000);
        this.clearMessagesAfterDelay();
      },
      error: (error) => {
        console.error('Erreur remboursement paiement:', error);
        const message = error?.message || 'Impossible de rembourser le paiement';
        this.errorMessage = message;
        this.notificationService.error(message, 5000);
        this.clearMessagesAfterDelay();
      }
    });
  }

  cancelBooking(booking: AdminBooking): void {
    if (!booking.reservationId) {
      const message = 'ID de réservation manquant';
      this.errorMessage = message;
      this.notificationService.error(message, 5000);
      this.clearMessagesAfterDelay();
      return;
    }
    
    this.adminService.cancelBooking(booking.reservationId).subscribe({
      next: (updatedBooking) => {
        booking.status = BookingManagement.normalizeStatus(updatedBooking.status);
        const message = 'Réservation annulée avec succès';
        this.successMessage = message;
        this.notificationService.success(message, 4000);
        this.clearMessagesAfterDelay();
        this.loadBookingStats(); // Recharger les stats
      },
      error: (error) => {
        console.error('Erreur annulation:', error);
        const message = error?.message || 'Erreur lors de l\'annulation';
        this.errorMessage = message;
        this.notificationService.error(message, 5000);
        this.clearMessagesAfterDelay();
      }
    });
  }

  completeBooking(booking: AdminBooking): void {
    if (!booking.reservationId) {
      const message = 'ID de réservation manquant';
      this.errorMessage = message;
      this.notificationService.error(message, 5000);
      this.clearMessagesAfterDelay();
      return;
    }
    
    this.adminService.updateBookingStatus(booking.reservationId, 'COMPLETED').subscribe({
      next: (updatedBooking) => {
        booking.status = BookingManagement.normalizeStatus(updatedBooking.status);
        const message = 'Réservation marquée comme terminée';
        this.successMessage = message;
        this.notificationService.success(message, 4000);
        this.clearMessagesAfterDelay();
        this.loadBookingStats(); // Recharger les stats
      },
      error: (error) => {
        console.error('Erreur completion:', error);
        const message = error?.message || 'Erreur lors du marquage comme terminée';
        this.errorMessage = message;
        this.notificationService.error(message, 5000);
        this.clearMessagesAfterDelay();
      }
    });
  }

  confirmDelete(booking: AdminBooking): void {
    this.selectedBooking = booking;
    this.isDeleteModalOpen = true;
    this.isDeleteProcessing = false;
  }

  deleteBooking(): void {
    if (!this.selectedBooking?.reservationId) {
      const message = 'ID de réservation manquant';
      this.errorMessage = message;
      this.notificationService.error(message, 5000);
      this.clearMessagesAfterDelay();
      return;
    }

    this.isDeleteProcessing = true;
    this.adminService.deleteBooking(this.selectedBooking.reservationId).subscribe({
      next: () => {
        this.bookings = this.bookings.filter(b => b.reservationId !== this.selectedBooking!.reservationId);
        this.totalElements = Math.max(0, this.totalElements - 1);
        this.applyFilters();
        const message = 'Réservation supprimée avec succès';
        this.successMessage = message;
        this.notificationService.success(message, 4000);
        this.isDeleteModalOpen = false;
        this.isDeleteProcessing = false;
        this.clearMessagesAfterDelay();
        this.loadBookingStats(); // Recharger les stats
      },
      error: (error) => {
        console.error('Erreur suppression:', error);
        const message = error?.message || 'Erreur lors de la suppression';
        this.errorMessage = message;
        this.notificationService.error(message, 5000);
        this.isDeleteProcessing = false;
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

  getPaymentStatusText(status?: string): string {
    if (!status) {
      return 'Paiement en attente';
    }
    const map: Record<string, string> = {
      PENDING: 'Paiement en attente',
      REQUIRES_ACTION: 'Action requise',
      PAID: 'Payé',
      FAILED: 'Échec du paiement',
      REFUNDED: 'Remboursé',
      EXPIRED: 'Paiement expiré'
    };
    return map[status.toUpperCase()] || status;
  }

  getPaymentBadgeClass(status?: string): string {
    switch (status?.toUpperCase()) {
      case 'PAID':
        return 'payment-paid';
      case 'FAILED':
        return 'payment-failed';
      case 'REFUNDED':
        return 'payment-refunded';
      case 'REQUIRES_ACTION':
        return 'payment-action';
      case 'EXPIRED':
        return 'payment-expired';
      default:
        return 'payment-pending';
    }
  }

  getEventStatusBadgeClass(status?: string): string {
    const normalized = status?.toUpperCase() ?? '';
    if (['PROCESSED', 'SUCCESS', 'PAID'].includes(normalized)) {
      return 'badge badge-success';
    }
    if (['FAILED', 'ERROR'].includes(normalized)) {
      return 'badge badge-danger';
    }
    if (['PENDING_RESERVATION', 'REQUIRES_ACTION', 'ACTION_REQUIRED'].includes(normalized)) {
      return 'badge badge-warning';
    }
    if (normalized === 'IGNORED') {
      return 'badge badge-muted';
    }
    return 'badge badge-info';
  }

  getAdminActionBadgeClass(actionType?: string): string {
    const type = actionType?.toUpperCase() ?? '';
    if (type.includes('REFUND') || type.includes('CANCEL')) {
      return 'badge badge-danger';
    }
    if (type.includes('CONFIRM') || type.includes('COMPLETE')) {
      return 'badge badge-success';
    }
    if (type.includes('EXPIRE') || type.includes('SUSPEND')) {
      return 'badge badge-warning';
    }
    return 'badge badge-info';
  }

  isPaymentActionLoading(reservationId?: number): boolean {
    if (!reservationId) {
      return false;
    }
    return !!this.paymentActionLoading[reservationId];
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

  formatPercentage(value?: number): string {
    if (value == null) {
      return '—';
    }
    return new Intl.NumberFormat('fr-FR', {
      style: 'percent',
      minimumFractionDigits: 1,
      maximumFractionDigits: 1
    }).format(value);
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

  private createEmptyStats(): BookingStatsOverview {
    return {
      totalBookings: 0,
      pendingBookings: 0,
      confirmedBookings: 0,
      cancelledBookings: 0,
      completedBookings: 0,
      totalRevenue: 0,
      outstandingRevenue: 0,
      monthToDateRevenue: 0,
      confirmationRate: 0
    };
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
    return BookingManagement.normalizeStatus(booking.status) === 'PENDING'
      && this.isPaymentPaid(booking);
  }

  private isPaymentPaid(booking: AdminBooking | null | undefined): boolean {
    if (!booking) {
      return false;
    }
    const paymentStatus = booking.paymentStatus?.toUpperCase();
    return !paymentStatus || paymentStatus === 'PAID';
  }

  // vérifier le statut de paiement avant d’afficher le nouveau bouton.
  canConfirmPaid(booking: AdminBooking | null | undefined): boolean {
    if (!booking) {
      return false;
    }
    return BookingManagement.normalizeStatus(booking.status) === 'PENDING'
      && booking.paymentStatus?.toUpperCase() === 'PAID';
  }

  canCancel(booking: AdminBooking): boolean {
    const status = BookingManagement.normalizeStatus(booking.status);
    return status === 'PENDING' || status === 'CONFIRMED';
  }

  canComplete(booking: AdminBooking): boolean {
    return BookingManagement.normalizeStatus(booking.status) === 'CONFIRMED';
  }

  canForceExpire(booking: AdminBooking): boolean {
    if (!booking.reservationId) {
      return false;
    }
    const paymentStatus = booking.paymentStatus?.toUpperCase();
    return paymentStatus === undefined || paymentStatus === 'PENDING' || paymentStatus === 'REQUIRES_ACTION';
  }

  canForceRefund(booking: AdminBooking): boolean {
    if (!booking.reservationId) {
      return false;
    }
    return booking.paymentStatus?.toUpperCase() === 'PAID';
  }

  canForceConfirm(booking: AdminBooking): boolean {
    return BookingManagement.normalizeStatus(booking.status) === 'PENDING'
      && !!booking.paymentStatus
      && booking.paymentStatus.toUpperCase() !== 'PAID';
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

  viewUserBookings(userId?: number, userName?: string): void {
    if (!userId) {
      return;
    }
    const label = userName?.trim().length ? userName : `Utilisateur #${userId}`;
    this.activeUserFilter = { id: userId, name: label };
    this.applyFilters();
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { userId, userName: label },
      queryParamsHandling: 'merge'
    });
    this.closeDetailModal();
  }

  private applyBookingPaymentUpdate(target: AdminBooking, updated: AdminBooking): void {
    target.paymentStatus = updated.paymentStatus ?? target.paymentStatus;
    target.paymentReference = updated.paymentReference ?? target.paymentReference;
    target.paymentDate = updated.paymentDate ?? target.paymentDate;
    target.status = BookingManagement.normalizeStatus(updated.status);
    target.totalPrice = updated.totalPrice ?? target.totalPrice;
  }

  loadActionAlerts(): void {
    this.actionAlertsLoading = true;
    this.adminService.getFinanceAlerts(this.buildActionAlertFilters()).pipe(takeUntil(this.destroy$)).subscribe({
      next: (alerts) => {
        this.actionAlerts = alerts;
        this.actionAlertsLoading = false;
        this.actionAlertError = '';
      },
      error: (error) => {
        console.error('Erreur chargement alertes de paiement:', error);
        this.actionAlertError = 'Impossible de charger les alertes de paiement';
        this.actionAlertsLoading = false;
      }
    });
  }

  refreshActionAlerts(): void {
    this.loadActionAlerts();
  }

  setActionAlertSeverity(filter: 'ALL' | 'ALERTE' | 'CRITIQUE'): void {
    this.actionAlertSeverity = filter;
    this.loadActionAlerts();
  }

  toggleActionAlertStatus(status: string): void {
    this.actionAlertStatuses[status] = !this.actionAlertStatuses[status];
    this.loadActionAlerts();
  }

  applyActionAlertSearch(): void {
    this.loadActionAlerts();
  }

  resetActionAlertFilters(): void {
    this.actionAlertSeverity = 'ALL';
    this.actionAlertSearch = '';
    this.actionAlertStatuses = {
      PENDING: true,
      REQUIRES_ACTION: true,
      FAILED: true,
      EXPIRED: false
    };
    this.loadActionAlerts();
  }

  downloadActionAlertsCsv(): void {
    this.adminService.exportFinanceCsv(this.alertExportRange, 'alerts', this.buildActionAlertFilters())
      .subscribe(blob => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = 'anomalies-paiement.csv';
        link.click();
        window.URL.revokeObjectURL(url);
      });
  }

  private buildActionAlertFilters(): FinanceAlertFilters {
    const statuses = Object.keys(this.actionAlertStatuses).filter(status => this.actionAlertStatuses[status]);
    const filters: FinanceAlertFilters = {
      actionRequiredOnly: true,
      statuses,
      limit: 40
    };
    if (this.actionAlertSeverity !== 'ALL') {
      filters.severity = this.actionAlertSeverity;
    }
    if (this.actionAlertSearch.trim()) {
      filters.search = this.actionAlertSearch.trim();
    }
    return filters;
  }

  private confirmAdminAction(message: string): boolean {
    if (typeof window === 'undefined' || typeof window.confirm !== 'function') {
      return true;
    }
    return window.confirm(message);
  }

  private collectOptionalReason(promptMessage: string): string | undefined {
    if (typeof window === 'undefined' || typeof window.prompt !== 'function') {
      return undefined;
    }
    const result = window.prompt(promptMessage);
    return result && result.trim().length > 0 ? result.trim() : undefined;
  }

  // 🆕 MÉTHODES POUR LES FILTRES ACTIFS
  hasActiveFilters(): boolean {
    return this.activeUserFilter !== null || 
           this.statusFilter !== 'ALL' || 
           this.dateFilter !== '' ||
           this.showAnomaliesOnly ||
           this.identityFilter !== 'ALL';
  }

  getActiveUserFilter(): { id: number, name: string } | null {
    return this.activeUserFilter;
  }

  hasMultipleFilters(): boolean {
    let count = 0;
    if (this.activeUserFilter) count++;
    if (this.statusFilter !== 'ALL') count++;
    if (this.dateFilter) count++;
    if (this.showAnomaliesOnly) count++;
    if (this.identityFilter !== 'ALL') count++;
    return count > 1;
  }

  toggleAnomalyFilter(): void {
    this.showAnomaliesOnly = !this.showAnomaliesOnly;
    this.applyFilters();
  }

  setIdentityFilter(filter: 'ALL' | 'VERIFIED' | 'UNVERIFIED'): void {
    this.identityFilter = filter;
    this.applyFilters();
  }

  clearIdentityFilter(): void {
    this.identityFilter = 'ALL';
    this.applyFilters();
  }

  getIdentityFilterLabel(): string {
    switch (this.identityFilter) {
      case 'VERIFIED':
        return 'Identité vérifiée';
      case 'UNVERIFIED':
        return 'Identité non vérifiée';
      default:
        return 'Toutes';
    }
  }

  getIdentityLabel(status?: string | null): string {
    const value = status?.toUpperCase() ?? 'NONE';
    switch (value) {
      case 'VERIFIED':
        return 'Identité vérifiée';
      case 'PROCESSING':
        return 'Analyse en cours';
      case 'REQUIRES_INPUT':
        return 'Documents incomplets';
      case 'REJECTED':
        return 'Documents rejetés';
      case 'PENDING':
      case 'NONE':
      default:
        return 'Identité non vérifiée';
    }
  }

  getIdentityBadgeClass(status?: string | null): string {
    const value = status?.toUpperCase() ?? 'NONE';
    if (value === 'VERIFIED') {
      return 'identity-chip success';
    }
    if (value === 'PROCESSING') {
      return 'identity-chip info';
    }
    if (value === 'REQUIRES_INPUT' || value === 'REJECTED') {
      return 'identity-chip danger';
    }
    return 'identity-chip muted';
  }

  private isIdentityVerifiedStatus(status?: string | null): boolean {
    return (status ?? '').toUpperCase() === 'VERIFIED';
  }

  formatIdentityTimestamp(timestamp?: string | null): string {
    if (!timestamp) {
      return '—';
    }
    return new Date(timestamp).toLocaleString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  buildIdentityReminderLink(booking?: AdminBooking | null): string | null {
    const email = booking?.user?.email;
    if (!email) {
      return null;
    }
    const profileUrl = typeof window !== 'undefined'
      ? `${window.location.origin}/profile/identity`
      : '/profile/identity';
    const userLabel = booking?.user?.username ?? `client #${booking?.user?.id ?? ''}`;
    const subject = encodeURIComponent('Vérification d’identité requise');
    const body = encodeURIComponent(
      `Bonjour ${userLabel},\n\nNous avons besoin d'une pièce d'identité valide pour finaliser la réservation #${booking?.reservationId ?? ''}. ` +
      `Merci de compléter vos documents ici : ${profileUrl}\n\nÀ très vite,\nL'équipe LocationUp`
    );
    return `mailto:${email}?subject=${subject}&body=${body}`;
  }

  shouldShowIdentityReminder(booking?: AdminBooking | null): boolean {
    return !this.isIdentityVerifiedStatus(booking?.identityStatus);
  }

  clearAnomalyFilter(): void {
    this.showAnomaliesOnly = false;
    this.applyFilters();
  }

  downloadBookingsCsv(): void {
    const filters = this.buildBookingExportFilters();
    this.adminService.exportBookingsCsv(filters).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const anchor = document.createElement('a');
        anchor.href = url;
        anchor.download = 'reservations-export.csv';
        anchor.click();
        window.URL.revokeObjectURL(url);
      },
      error: (error) => {
        console.error('Erreur export CSV réservations:', error);
        this.notificationService.error('Export impossible. Réessayez plus tard.');
      }
    });
  }

  private buildBookingExportFilters(): BookingExportFilters {
    const filters: BookingExportFilters = {};
    if (this.searchQuery.trim()) {
      filters.query = this.searchQuery.trim();
    }
    if (this.activeUserFilter) {
      filters.userId = this.activeUserFilter.id;
    }
    if (this.statusFilter !== 'ALL') {
      filters.status = this.statusFilter;
    }
    if (this.dateFilter) {
      filters.startDate = this.dateFilter;
      filters.endDate = this.dateFilter;
    }
    if (this.showAnomaliesOnly) {
      filters.anomaliesOnly = true;
    }
    return filters;
  }

  get isDetailModalProcessing(): boolean {
    return this.detailAuxLoading || this.paymentEventsLoading || this.adminActionsLoading;
  }

  private isBookingAnomaly(booking: AdminBooking): boolean {
    const status = booking.paymentStatus?.toUpperCase();
    if (!status) {
      return false;
    }
    return status === 'PENDING' || status === 'REQUIRES_ACTION' || status === 'FAILED' || status === 'EXPIRED';
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
    this.showAnomaliesOnly = false;
    this.identityFilter = 'ALL';
    this.router.navigate(['/admin/bookings']);
    this.applyFilters();
  }
}
