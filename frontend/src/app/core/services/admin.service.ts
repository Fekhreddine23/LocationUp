import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { AdminStats } from '../models/AdminStats.model';
import { AdminUser } from '../models/AdminUser.model';
import { RecentActivity } from '../models/RecentActivity.model';
import { UserResponse } from '../models/UserResponse.model';
import { OfferResponse } from '../models/OfferReponse.model';
import { CreateOfferRequest, Offer, OfferStatus } from '../models/offer.model';
import { AdminBooking, BookingResponse, BookingExportFilters, PaymentStatus } from '../models/AdminBooking.model';
import { DashboardTrends } from '../models/DashboardTrends.model';
import { FinanceAlertFilters, FinanceOverview, PaymentAlert, PaymentEventLogEntry } from '../models/admin-finance.model';
import { PaymentEvent } from '../models/payment-event.model';
import { ReservationAdminAction } from '../models/reservation-admin-action.model';
import { BusinessEventsService } from './business-events/business-events';
import { AuthService } from './auth.service';




@Injectable({
  providedIn: 'root'
})
export class AdminService {

    constructor(private http: HttpClient, private businessEvents: BusinessEventsService, private authService: AuthService) {}


     private apiUrl = 'http://localhost:8088/api/admin';


  /**
   * Récupère les statistiques admin
   * GET /api/admin/stats
   */
  getAdminStats(): Observable<AdminStats> {
    return this.http.get<Partial<AdminStats>>(`${this.apiUrl}/stats`).pipe(
      map(response => this.transformStats(response)),
      catchError(error => {
        console.error('Erreur API stats:', error);
        return of(this.getMockStats());
      })
    );
  }

  getDashboardTrends(months: number = 6): Observable<DashboardTrends> {
    const params = new HttpParams().set('months', months.toString());
    return this.http.get<DashboardTrends>(`${this.apiUrl}/stats/trends`, { params }).pipe(
      catchError(error => {
        console.error('Erreur API trends:', error);
        return of({
          reservationsByMonth: [],
          offersByCategory: [],
          topPickupCities: []
        });
      })
    );
  }

  /**
   * Finance overview
   */
  getFinanceOverview(months: number = 6): Observable<FinanceOverview> {
    const params = new HttpParams().set('months', months.toString());
    return this.http.get<FinanceOverview>(`${this.apiUrl}/finance/overview`, { params });
  }

  getFinanceAlerts(filters?: FinanceAlertFilters): Observable<PaymentAlert[]> {
    let params = new HttpParams();
    if (filters?.severity) {
      params = params.set('severity', filters.severity);
    }
    if (filters?.statuses?.length) {
      filters.statuses.forEach(status => {
        params = params.append('statuses', status);
      });
    }
    if (filters?.search) {
      params = params.set('search', filters.search);
    }
    if (filters?.startDate) {
      params = params.set('startDate', filters.startDate);
    }
    if (filters?.endDate) {
      params = params.set('endDate', filters.endDate);
    }
    if (filters?.actionRequiredOnly) {
      params = params.set('actionRequiredOnly', 'true');
    }
    if (filters?.limit) {
      params = params.set('limit', filters.limit.toString());
    }
    return this.http.get<PaymentAlert[]>(`${this.apiUrl}/finance/alerts`, { params });
  }

  getFinanceEvents(limit: number = 20): Observable<PaymentEventLogEntry[]> {
    const params = new HttpParams().set('size', limit.toString());
    return this.http.get<PaymentEventLogEntry[]>(`${this.apiUrl}/finance/events`, { params });
  }

  exportFinanceCsv(months: number = 6, scope: 'overview' | 'alerts' = 'overview', filters?: FinanceAlertFilters): Observable<Blob> {
    let params = new HttpParams().set('months', months.toString()).set('type', scope);
    if (scope === 'alerts' && filters) {
      if (filters.severity) {
        params = params.set('severity', filters.severity);
      }
      if (filters.statuses?.length) {
        filters.statuses.forEach(status => params = params.append('statuses', status));
      }
      if (filters.search) {
        params = params.set('search', filters.search);
      }
      if (filters.startDate) {
        params = params.set('startDate', filters.startDate);
      }
      if (filters.endDate) {
        params = params.set('endDate', filters.endDate);
      }
      if (filters.actionRequiredOnly) {
        params = params.set('actionRequiredOnly', 'true');
      }
      if (filters.limit) {
        params = params.set('limit', filters.limit.toString());
      }
    }
    return this.http.get(`${this.apiUrl}/finance/export`, { params, responseType: 'blob' });
  }

  exportBookingsCsv(filters: BookingExportFilters): Observable<Blob> {
    let params = new HttpParams();
    if (filters.query) {
      params = params.set('query', filters.query);
    }
    if (filters.status) {
      params = params.set('status', filters.status);
    }
    if (filters.startDate) {
      params = params.set('startDate', filters.startDate);
    }
    if (filters.endDate) {
      params = params.set('endDate', filters.endDate);
    }
    if (filters.anomaliesOnly) {
      params = params.set('anomaliesOnly', 'true');
    }
    if (filters.userId) {
      params = params.set('userId', filters.userId.toString());
    }
    return this.http.get(`${this.apiUrl}/bookings/export`, {
      params,
      responseType: 'blob'
    });
  }

  /**
   * Récupère tous les utilisateurs avec pagination
   * GET /api/admin/users?page=0&size=10
   */
  getAllUsers(page: number = 0, size: number = 10): Observable<UserResponse> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<any>(`${this.apiUrl}/users-management`, { params }).pipe(
      map(response => this.normalizeUserResponse(response)),
      catchError(error => {
        console.error('Erreur API users:', error);
        return of(this.getMockUserResponse());
      })
    );
  }

  /**
   * Récupère l'activité récente
   * GET /api/admin/recent-activity
   */
  getRecentActivity(): Observable<RecentActivity[]> {
    return this.http.get<RecentActivity[]>(`${this.apiUrl}/recent-activity`).pipe(
      catchError(error => {
        console.error('Erreur API activity:', error);
        return of(this.getMockActivity());
      })
    );
  }

  // ==================== MÉTHODES UTILITAIRES ====================

  /**
   * Normalise les réponses utilisateur venant d'API hétérogènes
   */
  private normalizeUserResponse(response: any): UserResponse {
    const rawList = this.extractUserList(response);
    const content = rawList.map(user => this.transformUser(user));

    const totalElements = this.resolveTotalElementsValue(response?.totalElements, content.length);
    const size = this.resolvePageSize(response?.size, content.length);
    const totalPages = this.resolveTotalPages(response?.totalPages, totalElements, size);
    const number = typeof response?.number === 'number' ? response.number : 0;

    return {
      content,
      totalElements,
      totalPages,
      size,
      number
    };
  }

  private extractUserList(response: any): any[] {
    if (Array.isArray(response)) {
      return response;
    }
    if (Array.isArray(response?.content)) {
      return response.content;
    }
    if (Array.isArray(response?.users)) {
      return response.users;
    }
    if (Array.isArray(response?.data)) {
      return response.data;
    }
    return [];
  }

  private resolveTotalElementsValue(rawTotal: any, fallback: number): number {
    if (typeof rawTotal === 'number' && rawTotal > 0) {
      return rawTotal;
    }
    if (typeof rawTotal === 'number' && rawTotal === 0 && fallback === 0) {
      return 0;
    }
    return fallback;
  }

  private resolvePageSize(rawSize: any, fallback: number): number {
    if (typeof rawSize === 'number' && rawSize > 0) {
      return rawSize;
    }
    return fallback > 0 ? fallback : 10;
  }

  private resolveTotalPages(rawTotalPages: any, totalElements: number, size: number): number {
    if (typeof rawTotalPages === 'number' && rawTotalPages > 0) {
      return rawTotalPages;
    }
    const effectiveSize = size > 0 ? size : 1;
    return Math.max(1, Math.ceil(totalElements / effectiveSize));
  }

  private resolveUsername(username: string | undefined | null, email: string | undefined | null): string {
    if (username && username.trim().length > 0) {
      return username.trim();
    }
    if (email && email.trim().length > 0) {
      const localPart = email.split('@')[0];
      return localPart ? localPart.trim() : 'Utilisateur';
    }
    return 'Utilisateur';
  }

  /**
   * Transforme un utilisateur backend en format frontend
   */
  transformUser(backendUser: any): AdminUser {
    const rawUsername = backendUser?.username ?? backendUser?.userName ?? backendUser?.login;
    const rawEmail = backendUser?.email ?? backendUser?.mail;
    const resolvedUsername = this.resolveUsername(rawUsername, rawEmail);

    const firstName = backendUser?.firstName ?? backendUser?.firstname ?? backendUser?.first_name;
    const lastName = backendUser?.lastName ?? backendUser?.lastname ?? backendUser?.last_name;
    const status = backendUser?.status ?? backendUser?.userStatus ?? 'active';

    return {
      id: backendUser.id,
      username: resolvedUsername,
      email: rawEmail,
      role: backendUser.role?.name || backendUser.role || 'ROLE_USER',
      createdAt: backendUser.createdAt || backendUser.registrationDate || new Date().toISOString(),
      firstName,
      lastName,
      lastLogin: backendUser.lastLogin ?? backendUser.lastLoginAt,
      status,
      bookingCount: backendUser.bookingCount ?? backendUser.totalBookings ?? backendUser.reservationCount ?? 0
    };
  }

  private transformStats(response: Partial<AdminStats>): AdminStats {
    return {
      totalUsers: response.totalUsers ?? 0,
      activeUsers: response.activeUsers ?? 0,
      totalReservations: response.totalReservations ?? (response as any).totalBookings ?? 0,
      pendingReservations: response.pendingReservations ?? (response as any).pendingBookings ?? 0,
      totalOffers: response.totalOffers ?? 0,
      totalRevenue: response.totalRevenue ?? (response as any).revenue ?? 0
    };
  }

  private transformBookingResponse(response: any): BookingResponse {
    const content = Array.isArray(response?.content)
      ? response.content
      : Array.isArray(response?.bookings)
        ? response.bookings
        : Array.isArray(response)
          ? response
          : [];

    const totalElements = response?.totalElements ?? content.length ?? 0;
    const size = response?.size ?? content.length ?? 0;

    return {
      content: content.map((booking: any) => this.transformBooking(booking)),
      totalElements,
      totalPages: response?.totalPages ?? (size ? Math.max(1, Math.ceil(totalElements / size)) : 1),
      size,
      number: response?.number ?? 0
    };
  }

  private transformBooking(rawBooking: any): AdminBooking {
    if (!rawBooking) {
      return {
        reservationId: 0,
        reservationDate: new Date().toISOString(),
        status: 'PENDING'
      };
    }

    const user = this.mapBookingUser(rawBooking);
    const offer = this.mapBookingOffer(rawBooking);
    const reservationDate = rawBooking.reservationDate
      ?? rawBooking.date
      ?? rawBooking.reservation_datetime
      ?? new Date().toISOString();
    const createdAt = rawBooking.createdAt
      ?? rawBooking.created_at
      ?? rawBooking.creationDate
      ?? rawBooking.createdDate
      ?? reservationDate;

    return {
      reservationId: rawBooking.reservationId ?? rawBooking.id ?? 0,
      reservationDate,
      status: this.normalizeBookingStatus(rawBooking.status),
      createdAt,
      totalPrice: rawBooking.totalPrice ?? rawBooking.price ?? offer?.price,
      paymentStatus: this.normalizePaymentStatus(rawBooking.paymentStatus ?? rawBooking.payment_status),
      paymentReference: rawBooking.paymentReference ?? rawBooking.payment_reference ?? undefined,
      paymentDate: rawBooking.paymentDate ?? rawBooking.payment_date ?? undefined,
      user,
      offer
    };
  }

  private mapBookingUser(rawBooking: any): AdminBooking['user'] | undefined {
    const rawUser = rawBooking.user
      ?? rawBooking.customer
      ?? rawBooking.client
      ?? rawBooking.account
      ?? rawBooking.userAccount
      ?? null;
    const fallbackId = rawUser?.id ?? rawBooking.userId ?? rawBooking.customerId ?? rawBooking.clientId;

    if (!rawUser && !fallbackId) {
      return undefined;
    }

    const username =
      rawUser?.username
      ?? rawUser?.login
      ?? rawUser?.name
      ?? rawBooking.username
      ?? rawBooking.userName
      ?? (fallbackId ? `Utilisateur #${fallbackId}` : undefined);

    const email =
      rawUser?.email
      ?? rawUser?.emailAddress
      ?? rawBooking.email
      ?? rawBooking.userEmail;

    return {
      id: fallbackId ?? 0,
      username: username ?? 'Utilisateur',
      email: email ?? undefined
    };
  }

  private mapBookingOffer(rawBooking: any): AdminBooking['offer'] | undefined {
    const rawOffer = rawBooking.offer
      ?? rawBooking.offerDetails
      ?? rawBooking.offerDto
      ?? null;
    const fallbackOfferId = rawOffer?.offerId ?? rawOffer?.id ?? rawBooking.offerId;

    if (!rawOffer && !fallbackOfferId) {
      return undefined;
    }

    const mobilityServiceName =
      rawOffer?.mobilityService?.name
      ?? rawOffer?.mobilityServiceName
      ?? rawOffer?.serviceName
      ?? rawOffer?.name
      ?? (typeof rawOffer?.mobilityService === 'string' ? rawOffer.mobilityService : undefined)
      ?? rawBooking.mobilityService
      ?? rawBooking.serviceName;

    const mobilityServiceCategory =
      rawOffer?.mobilityService?.categorie
      ?? rawOffer?.mobilityService?.category
      ?? rawOffer?.categorie
      ?? rawOffer?.category
      ?? rawOffer?.mobilityServiceCategory;

    const pickupLocationName =
      rawOffer?.pickupLocation?.name
      ?? rawOffer?.pickupLocationName
      ?? rawOffer?.pickupLocation
      ?? rawBooking.pickupLocationName
      ?? rawBooking.pickupLocation;

    const price =
      rawOffer?.price
      ?? rawBooking.totalPrice
      ?? rawBooking.price;

    return {
      offerId: fallbackOfferId ?? 0,
      mobilityService: this.buildMobilityServiceLabel(mobilityServiceName, mobilityServiceCategory),
      pickupLocation: pickupLocationName ?? undefined,
      price
    };
  }

  private buildMobilityServiceLabel(name?: string, category?: string): string | undefined {
    const cleanName = (name ?? '').trim();
    const cleanCategory = (category ?? '').trim();

    if (cleanName && cleanCategory) {
      return `${cleanName} (${cleanCategory})`;
    }

    return cleanName || cleanCategory || undefined;
  }

  private normalizeBookingStatus(status?: string | null): AdminBooking['status'] {
    if (!status) {
      return 'PENDING';
    }

    const upper = status.toUpperCase();
    const allowed: AdminBooking['status'][] = ['PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED'];
    return allowed.includes(upper as AdminBooking['status'])
      ? (upper as AdminBooking['status'])
      : 'PENDING';
  }

  private normalizePaymentStatus(status?: string | null): PaymentStatus | undefined {
    if (!status) {
      return undefined;
    }
    const upper = status.toUpperCase();
    const allowed: PaymentStatus[] = ['PENDING', 'REQUIRES_ACTION', 'PAID', 'FAILED', 'REFUNDED', 'EXPIRED'];
    return allowed.includes(upper as PaymentStatus) ? (upper as PaymentStatus) : undefined;
  }

  private transformOffer(rawOffer: any): Offer {
    const pickupLocationName = rawOffer.pickupLocation?.name ?? rawOffer.pickupLocationName ?? rawOffer.pickupLocation ?? '';
    const returnLocationName = rawOffer.returnLocation?.name ?? rawOffer.returnLocationName ?? rawOffer.returnLocation ?? '';
    const mobilityServiceName = rawOffer.mobilityService?.name ?? rawOffer.mobilityServiceName ?? rawOffer.mobilityService ?? '';
    const pickupLatitude = rawOffer.pickupLatitude ?? rawOffer.pickupLocation?.latitude ?? rawOffer.pickupLocationLat;
    const pickupLongitude = rawOffer.pickupLongitude ?? rawOffer.pickupLocation?.longitude ?? rawOffer.pickupLocationLng;
    const returnLatitude = rawOffer.returnLatitude ?? rawOffer.returnLocation?.latitude ?? rawOffer.returnLocationLat;
    const returnLongitude = rawOffer.returnLongitude ?? rawOffer.returnLocation?.longitude ?? rawOffer.returnLocationLng;

    return {
      offerId: rawOffer.offerId ?? rawOffer.id,
      description: rawOffer.description ?? '',
      price: rawOffer.price ?? 0,
      pickupDatetime: rawOffer.pickupDatetime ?? rawOffer.pickupDate ?? new Date().toISOString(),
      createdAt: rawOffer.createdAt ?? new Date().toISOString(),
      updatedAt: rawOffer.updatedAt ?? rawOffer.createdAt ?? new Date().toISOString(),
      version: rawOffer.version ?? 0,
      adminId: rawOffer.adminId ?? rawOffer.admin?.id ?? undefined,
      mobilityServiceId: rawOffer.mobilityServiceId ?? rawOffer.mobilityService?.id ?? undefined,
      pickupLocationId: rawOffer.pickupLocationId ?? rawOffer.pickupLocation?.id ?? undefined,
      returnLocationId: rawOffer.returnLocationId ?? rawOffer.returnLocation?.id ?? undefined,
      pickupLocationName,
      returnLocationName,
      pickupLocationCity: rawOffer.pickupLocationCity ?? pickupLocationName,
      returnLocationCity: rawOffer.returnLocationCity ?? returnLocationName,
      pickupLatitude,
      pickupLongitude,
      returnLatitude,
      returnLongitude,
      status: this.normalizeOfferStatus(rawOffer.status),
      mobilityService: mobilityServiceName,
      pickupLocation: pickupLocationName,
      returnLocation: returnLocationName,
      adminName: rawOffer.admin?.username ?? rawOffer.adminName
    };
  }

  private buildOfferUpdatePayload(offerData: Partial<Offer> | undefined): Partial<Offer> {
    if (!offerData) {
      return {};
    }
    const {
      offerId,
      mobilityService,
      pickupLocation,
      returnLocation,
      adminName,
      ...rest
    } = offerData;
    return rest;
  }

  private normalizeOfferStatus(status?: string | null): OfferStatus {
    if (!status) {
      return 'PENDING';
    }
    const upper = status.toUpperCase();
    const allowed: OfferStatus[] = ['PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED'];
    return allowed.includes(upper as OfferStatus) ? (upper as OfferStatus) : 'PENDING';
  }

  /**
   * Formate une date pour l'affichage
   */
  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('fr-FR', {
      day: 'numeric',
      month: 'long',
      year: 'numeric'
    });
  }

  /**
   * Formate un montant en euros
   */
  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: 'EUR'
    }).format(amount);
  }



   //====================== RESERVATIONS ADMIN ======================

    /**
   * Récupère toutes les réservations
   * GET /api/admin/bookings
   */
  getAllBookings(page: number = 0, size: number = 10): Observable<BookingResponse> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<BookingResponse>(`${this.apiUrl}/bookings`, { params }).pipe(
      map(response => this.transformBookingResponse(response)),
      catchError(error => {
        console.error('Erreur chargement réservations:', error);
        return of(this.getMockBookingResponse());
      })
    );
  }

  /**
   * Récupère une réservation par son ID
   * GET /api/admin/bookings/{id}
   */
  getBookingById(bookingId: number): Observable<AdminBooking> {
    return this.http.get<any>(`${this.apiUrl}/bookings/${bookingId}`).pipe(
      map(booking => this.transformBooking(booking)),
     catchError(error => this.handleApiError('récupération de la réservation', error))
    );
  }

  /**
   * Met à jour le statut d'une réservation
   * POST /api/admin/bookings/{id}/status
   */
  updateBookingStatus(bookingId: number, newStatus: string): Observable<AdminBooking> {
    return this.http.post<any>(`${this.apiUrl}/bookings/${bookingId}/status`, { status: newStatus }).pipe(
      map(booking => this.transformBooking(booking)),
      catchError(error => this.handleApiError('mise à jour du statut de réservation', error))
    );
  }

  /**
   * Crée une session de paiement Stripe pour une réservation existante
   * POST /api/admin/bookings/{id}/payment-session
   */
  createBookingPaymentSession(bookingId: number, successUrl?: string, cancelUrl?: string): Observable<{ paymentUrl: string; sessionId: string }> {
    const payload = {
      successUrl: successUrl ?? `${window.location.origin}/admin/bookings?payment=success`,
      cancelUrl: cancelUrl ?? `${window.location.origin}/admin/bookings?payment=cancel`
    };

    return this.http.post<{ paymentUrl: string; sessionId: string }>(
      `${this.apiUrl}/bookings/${bookingId}/payment-session`,
      payload
    ).pipe(
      catchError(error => this.handleApiError('création de la session de paiement', error))
    );
  }

  /**
   * Force l'expiration d'un paiement côté admin
   * POST /api/admin/bookings/{id}/payment/expire
   */
  forcePaymentExpiration(bookingId: number, reason?: string | null): Observable<AdminBooking> {
    const payload = reason ? { reason } : {};
    return this.http.post<any>(`${this.apiUrl}/bookings/${bookingId}/payment/expire`, payload).pipe(
      map(booking => this.transformBooking(booking)),
      catchError(error => this.handleApiError('expiration manuelle du paiement', error))
    );
  }

  /**
   * Force un remboursement (statut REFUNDED) côté admin
   * POST /api/admin/bookings/{id}/payment/refund
   */
  forcePaymentRefund(bookingId: number, reason?: string | null): Observable<AdminBooking> {
    const payload = reason ? { reason } : {};
    return this.http.post<any>(`${this.apiUrl}/bookings/${bookingId}/payment/refund`, payload).pipe(
      map(booking => this.transformBooking(booking)),
      catchError(error => this.handleApiError('remboursement manuel', error))
    );
  }

  getBookingPaymentEvents(bookingId: number): Observable<PaymentEvent[]> {
    return this.http.get<PaymentEvent[]>(`${this.apiUrl}/bookings/${bookingId}/payment/events`).pipe(
      catchError(error => this.handleApiError('historique des événements de paiement', error))
    );
  }

  getBookingAdminActions(bookingId: number): Observable<ReservationAdminAction[]> {
    return this.http.get<ReservationAdminAction[]>(`${this.apiUrl}/bookings/${bookingId}/admin-actions`).pipe(
      catchError(error => this.handleApiError('historique des actions admin', error))
    );
  }

  /**
   * Annule une réservation
   * POST /api/admin/bookings/{id}/cancel
   */
  cancelBooking(bookingId: number): Observable<AdminBooking> {
    return this.http.post<any>(`${this.apiUrl}/bookings/${bookingId}/cancel`, {}).pipe(
      map(booking => this.transformBooking(booking)),
       catchError(error => this.handleApiError('annulation de la réservation', error))
    );
  }

  /**
   * Supprime une réservation
   * DELETE /api/admin/bookings/{id}
   */
  deleteBooking(bookingId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/bookings/${bookingId}`).pipe(
       catchError(error => this.handleApiError('suppression de la réservation', error))
    );
  }

  /**
   * Recherche des réservations
   * GET /api/admin/bookings/search?query=...
   */
  searchBookings(query: string, page: number = 0, size: number = 10): Observable<BookingResponse> {
    const params = new HttpParams()
      .set('query', query)
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<BookingResponse>(`${this.apiUrl}/bookings/search`, { params }).pipe(
      map(response => this.transformBookingResponse(response)),
      catchError(error => {
        console.error('Erreur recherche réservations:', error);
        return of(this.getMockBookingResponse());
      })
    );
  }

  /**
   * Statistiques des réservations
   * GET /api/admin/bookings/stats
   */
  getBookingStats(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/bookings/stats`).pipe(
      catchError(error => {
        console.error('Erreur stats réservations:', error);
        return of(this.getMockBookingStats());
      })
    );
  }

  // Données mockées pour les réservations
  private getMockBookingResponse(): BookingResponse {
    const mockBookings: AdminBooking[] = [
      {
        reservationId: 1,
        user: {
          id: 2,
          username: 'john_doe',
          email: 'john@example.com'
        },
        offer: {
          offerId: 1,
          mobilityService: 'Voiture électrique',
          pickupLocation: 'Paris Centre',
          price: 89.99
        },
        reservationDate: '2024-12-25T10:00:00',
        status: 'CONFIRMED',
        createdAt: '2024-11-15T14:30:00',
        totalPrice: 89.99
      },
      {
        reservationId: 2,
        user: {
          id: 3,
          username: 'marie_dupont',
          email: 'marie@example.com'
        },
        offer: {
          offerId: 2,
          mobilityService: 'Scooter électrique',
          pickupLocation: 'Lyon Part-Dieu',
          price: 29.99
        },
        reservationDate: '2024-12-20T14:30:00',
        status: 'PENDING',
        createdAt: '2024-11-20T09:15:00',
        totalPrice: 29.99
      },
      {
        reservationId: 3,
        user: {
          id: 4,
          username: 'pierre_martin',
          email: 'pierre@example.com'
        },
        offer: {
          offerId: 3,
          mobilityService: 'Vélo électrique',
          pickupLocation: 'Marseille Vieux-Port',
          price: 19.99
        },
        reservationDate: '2024-12-18T09:00:00',
        status: 'CANCELLED',
        createdAt: '2024-11-10T16:45:00',
        totalPrice: 19.99
      },
      {
        reservationId: 4,
        user: {
          id: 2,
          username: 'john_doe',
          email: 'john@example.com'
        },
        offer: {
          offerId: 1,
          mobilityService: 'Voiture électrique',
          pickupLocation: 'Paris Centre',
          price: 89.99
        },
        reservationDate: '2024-12-28T15:00:00',
        status: 'COMPLETED',
        createdAt: '2024-11-18T11:20:00',
        totalPrice: 89.99
      }
    ];

    return {
      content: mockBookings,
      totalElements: mockBookings.length,
      totalPages: 1,
      size: 10,
      number: 0
    };
  }

  private getMockBookingStats(): any {
    return {
      totalBookings: 156,
      pendingBookings: 12,
      confirmedBookings: 89,
      cancelledBookings: 23,
      completedBookings: 32,
      totalRevenue: 12540.50,
      outstandingRevenue: 3200.10,
      monthToDateRevenue: 1850.40,
      confirmationRate: 0.62
    };
  }











  // ==================== DONNÉES MOCKÉES POUR LE DÉVELOPPEMENT ====================

  private getMockStats(): AdminStats {
    return {
      totalUsers: 154,
      activeUsers: 89,
      totalReservations: 423,
      pendingReservations: 12,
      totalOffers: 28,
      totalRevenue: 8450.5
    };
  }

  private getMockUserResponse(): UserResponse {
    return {
      content: [
        {
          id: 1,
          username: 'admin',
          email: 'admin@locationup.com',
          role: 'ROLE_ADMIN',
          createdAt: '2024-01-15T00:00:00',
          status: 'active',
          bookingCount: 0
        },
        {
          id: 2,
          username: 'john_doe',
          email: 'john@example.com',
          role: 'ROLE_USER',
          createdAt: '2024-02-20T00:00:00',
          firstName: 'John',
          lastName: 'Doe',
          lastLogin: '2024-03-25T10:30:00',
          status: 'active',
          bookingCount: 8
        },
        {
          id: 3,
          username: 'marie_dupont',
          email: 'marie.dupont@example.com',
          role: 'ROLE_USER',
          createdAt: '2024-03-10T00:00:00',
          firstName: 'Marie',
          lastName: 'Dupont',
          status: 'active',
          bookingCount: 3
        }
      ],
      totalElements: 3,
      totalPages: 1,
      size: 10,
      number: 0
    };
  }

  private getMockActivity(): RecentActivity[] {
    return [
      {
        id: 1,
        type: 'RESERVATION',
        description: 'Nouvelle réservation créée',
        timestamp: new Date().toISOString(),
        user: {
          id: 2,
          username: 'john_doe',
          email: 'john@example.com'
        }
      },
      {
        id: 2,
        type: 'USER',
        description: 'Nouvel utilisateur inscrit',
        timestamp: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(), // 2 heures ago
        user: {
          id: 3,
          username: 'marie_dupont',
          email: 'marie.dupont@example.com'
        }
      },
      {
        id: 3,
        type: 'RESERVATION',
        description: 'Réservation confirmée',
        timestamp: new Date(Date.now() - 5 * 60 * 60 * 1000).toISOString(), // 5 heures ago
        user: {
          id: 2,
          username: 'john_doe',
          email: 'john@example.com'
        }
      }
    ];
  }


  /**
   * Récupère un utilisateur par son ID
   * GET /api/admin/users/{id}
   */
  getUserById(userId: number): Observable<AdminUser> {
    return this.http.get<AdminUser>(`${this.apiUrl}/users-management/${userId}`).pipe(
      catchError(error => {
        console.error('Erreur récupération user:', error);
        throw error;
      })
    );
  }

  /**
   * Met à jour un utilisateur
   * PUT /api/admin/users/{id}
   */
  updateUser(userId: number, userData: any): Observable<AdminUser> {
    return this.http.put<any>(`${this.apiUrl}/users-management/${userId}`, userData).pipe(
      map(response => this.transformUser(response)),
      catchError(error => {
        console.error('Erreur mise à jour user:', error);
        throw error;
      })
    );
  }

  /**
   * Désactive un utilisateur
   * POST /api/admin/users/{id}/deactivate
   */
  deactivateUser(userId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/users-management/${userId}/deactivate`, {}).pipe(
      catchError(error => {
        console.error('Erreur désactivation user:', error);
        throw error;
      })
    );
  }

  /**
   * Réactive un utilisateur
   * POST /api/admin/users/{id}/activate
   */
  activateUser(userId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/users-management/${userId}/activate`, {}).pipe(
      catchError(error => {
        console.error('Erreur activation user:', error);
        throw error;
      })
    );
  }

  /**
   * Supprime un utilisateur
   * DELETE /api/admin/users/{id}
   */
  deleteUser(userId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/users-management/${userId}`).pipe(
      catchError(error => {
        console.error('Erreur suppression user:', error);
        throw error;
      })
    );
  }

  /**
 * Change le rôle d'un utilisateur
 * POST /api/admin/users-management/{id}/role
 */
changeUserRole(userId: number, newRole: string): Observable<AdminUser> {
  // Correction : utiliser "users-management" et le bon payload
  return this.http.post<any>(`${this.apiUrl}/users-management/${userId}/role`, { newRole: newRole }).pipe(
    map(response => this.transformUser(response)),
    catchError(error => {
      console.error('Erreur changement rôle:', error);
      throw error;
    })
  );
}

  /**
   * Recherche des utilisateurs
   * GET /api/admin/users/search?query=...
   */
  searchUsers(query: string, page: number = 0, size: number = 10): Observable<UserResponse> {
    const params = new HttpParams()
      .set('query', query)
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<any>(`${this.apiUrl}/users-management/search`, { params }).pipe(
      map(response => this.normalizeUserResponse(response)),
      catchError(error => {
        console.error('Erreur recherche users:', error);
        return of(this.getMockUserResponse());
      })
    );
  }


  /**
   * Récupère toutes les offres
   * GET /api/admin/offers
   */
  getAllOffers(page: number = 0, size: number = 10): Observable<OfferResponse> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<OfferResponse>(`${this.apiUrl}/offers`, { params }).pipe(
      map(response => ({
        ...response,
        content: response.content.map(offer => this.transformOffer(offer))
      })),
      catchError(error => {
        console.error('Erreur chargement offres:', error);
        return of(this.getMockOfferResponse());
      })
    );
  }


  /**
   * Récupère une offre par son ID
   * GET /api/admin/offers/{id}
   */
  getOfferById(offerId: number): Observable<Offer> {
    return this.http.get<Offer>(`${this.apiUrl}/offers/${offerId}`).pipe(
      map(response => this.transformOffer(response)),
      catchError(error => this.handleApiError('récupération de l\'offre', error))
    );
  }

  /**
   * Crée une nouvelle offre
   * POST /api/admin/offers
   */
  // ✅ MODIFIÉ : Créer une offre avec notification
  createOffer(offerData: CreateOfferRequest): Observable<Offer> {
    return this.http.post<Offer>(`${this.apiUrl}/offers`, offerData).pipe(
      tap((newOffer: Offer) => {
        // ✅ NOTIFICATION pour création d'offre par admin
        console.log('🎉 Offre créée par admin, envoi notification...');
        
        const currentUser = this.authService.currentUserValue;
        if (currentUser) {
          this.businessEvents.notifyOfferCreated(
            newOffer.offerId,
            newOffer.description,
            currentUser.id
          ).subscribe({
            next: () => console.log('✅ Notification offre créée envoyée'),
            error: (err) => console.error('❌ Erreur notification offre:', err)
          });
        }
      }),
      catchError(error => this.handleApiError('création de l\'offre', error))
    );
  }


   /**
   * Met à jour une offre
   * PUT /api/admin/offers/{id}
   */
  // ✅ MODIFIÉ : Mettre à jour une offre avec notification
  updateOffer(offerId: number, offerData: any): Observable<Offer> {
    const payload = this.buildOfferUpdatePayload(offerData);
    return this.http.put<Offer>(`${this.apiUrl}/offers/${offerId}`, payload).pipe(
      map(response => this.transformOffer(response)),
      tap((updatedOffer: Offer) => {
        // ✅ NOTIFICATION pour modification d'offre
        console.log('✏️ Offre modifiée par admin, envoi notification...');
        
        const currentUser = this.authService.currentUserValue;
        if (currentUser) {
          this.businessEvents.notifySystemEvent(
            'OFFER_UPDATED',
            `Offre "${updatedOffer.description.substring(0, 30)}..." modifiée`,
            'INFO',
            currentUser.id
          ).subscribe();
        }
      }),
      catchError(error => this.handleApiError('mise à jour de l\'offre', error))
    );
  }


  /**
   * Supprime une offre
   * DELETE /api/admin/offers/{id}
   */
  // ✅ MODIFIÉ : Supprimer une offre avec notification
  deleteOffer(offerId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/offers/${offerId}`).pipe(
      tap(() => {
        // ✅ NOTIFICATION pour suppression d'offre
        console.log('🗑️ Offre supprimée par admin, envoi notification...');
        
        const currentUser = this.authService.currentUserValue;
        if (currentUser) {
          this.businessEvents.notifySystemEvent(
            'OFFER_DELETED',
            `Offre #${offerId} supprimée`,
            'WARNING',
            currentUser.id
          ).subscribe();
        }
      }),
      catchError(error => this.handleApiError('suppression de l\'offre', error))
    );
  }

   /**
   * Change le statut d'une offre
   * POST /api/admin/offers/{id}/status
   */
   // ✅ MODIFIÉ : Changer statut offre avec notification
  changeOfferStatus(offerId: number, newStatus: string): Observable<Offer> {
    return this.http.post<Offer>(`${this.apiUrl}/offers/${offerId}/status`, { status: newStatus }).pipe(
      tap((updatedOffer: Offer) => {
        // ✅ NOTIFICATION pour changement de statut
        console.log('🔄 Statut offre changé, envoi notification...');
        
        const currentUser = this.authService.currentUserValue;
        if (currentUser) {
          const statusMessages = {
            'PENDING': 'en attente',
            'CONFIRMED': 'confirmée', 
            'CANCELLED': 'annulée',
            'COMPLETED': 'terminée'
          };
          
          const statusText = statusMessages[newStatus as keyof typeof statusMessages] || newStatus.toLowerCase();
          
          this.businessEvents.notifySystemEvent(
            'OFFER_STATUS_CHANGED',
            `Offre "${updatedOffer.description.substring(0, 30)}..." ${statusText}`,
            this.getSeverityForStatus(newStatus),
            currentUser.id
          ).subscribe();
        }
      }),
      catchError(error => this.handleApiError('changement de statut de l\'offre', error))
    );
  }

  /**
   * Recherche des offres
   * GET /api/admin/offers/search?query=...
   */
  searchOffers(query: string, page: number = 0, size: number = 10): Observable<OfferResponse> {
    const params = new HttpParams()
      .set('query', query)
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<OfferResponse>(`${this.apiUrl}/offers/search`, { params }).pipe(
      catchError(error => {
        console.error('Erreur recherche offres:', error);
        return of(this.getMockOfferResponse());
      })
    );
  }

  // Données mockées pour les offres
  private getMockOfferResponse(): OfferResponse {
    const mockOffers: Offer[] = [
      {
        offerId: 1,
        pickupDatetime: '2024-12-25T10:00:00',
        description: 'Voiture électrique premium avec autonomie 400km',
        price: 89.99,
        createdAt: '2024-01-15T00:00:00',
        updatedAt: '2024-02-01T00:00:00',
        version: 1,
        status: 'PENDING',
        mobilityService: 'Voiture électrique',
        pickupLocation: 'Paris Centre',
        pickupLocationName: 'Paris Centre',
        pickupLocationCity: 'Paris',
        returnLocation: 'Paris Centre',
        returnLocationName: 'Paris Centre',
        returnLocationCity: 'Paris',
        adminName: 'Admin System'
      },
      {
        offerId: 2,
        pickupDatetime: '2024-12-20T14:30:00',
        description: 'Scooter urbain parfait pour la ville',
        price: 29.99,
        createdAt: '2024-02-10T00:00:00',
        updatedAt: '2024-02-15T00:00:00',
        version: 2,
        status: 'CONFIRMED',
        mobilityService: 'Scooter électrique',
        pickupLocation: 'Lyon Part-Dieu',
        pickupLocationName: 'Lyon Part-Dieu',
        pickupLocationCity: 'Lyon',
        returnLocation: 'Lyon Perrache',
        returnLocationName: 'Lyon Perrache',
        returnLocationCity: 'Lyon',
        adminName: 'Admin System'
      },
      {
        offerId: 3,
        pickupDatetime: '2024-12-18T09:00:00',
        description: 'Vélo à assistance électrique pour visiter la ville',
        price: 19.99,
        createdAt: '2024-03-05T00:00:00',
        updatedAt: '2024-03-10T00:00:00',
        version: 1,
        status: 'CANCELLED',
        mobilityService: 'Vélo électrique',
        pickupLocation: 'Marseille Vieux-Port',
        pickupLocationName: 'Marseille Vieux-Port',
        pickupLocationCity: 'Marseille',
        returnLocation: 'Marseille Vieux-Port',
        returnLocationName: 'Marseille Vieux-Port',
        returnLocationCity: 'Marseille',
        adminName: 'Admin Sud'
      },
      {
        offerId: 4,
        pickupDatetime: '2025-01-05T08:30:00',
        description: 'Berline thermique confortable pour longs trajets',
        price: 59.5,
        createdAt: '2024-04-01T00:00:00',
        updatedAt: '2024-04-05T00:00:00',
        version: 1,
        status: 'COMPLETED',
        mobilityService: 'Voiture thermique',
        pickupLocation: 'Bordeaux Gare',
        pickupLocationName: 'Bordeaux Gare',
        pickupLocationCity: 'Bordeaux',
        returnLocation: 'Bordeaux Mérignac',
        returnLocationName: 'Bordeaux Mérignac',
        returnLocationCity: 'Bordeaux',
        adminName: 'Admin Ouest'
      }
    ];

    return {
      content: mockOffers,
      totalElements: mockOffers.length,
      totalPages: 1,
      size: 10,
      number: 0
    };
  }


  private handleApiError(operation: string, error: any): Observable<never> {
    console.error(`❌ Erreur ${operation}:`, error);

    let errorMessage = 'Erreur de connexion au serveur';

    if (error.status === 0) {
      errorMessage = 'Serveur inaccessible - vérifiez que le backend est démarré';
    } else if (error.status === 401) {
      errorMessage = 'Non authentifié - veuillez vous reconnecter';
    } else if (error.status === 403) {
      errorMessage = 'Accès non autorisé - droits administrateur requis';
    } else if (error.status === 404) {
      errorMessage = 'Endpoint non trouvé - vérifiez l\'URL';
    } else {
      const backendMessage =
        (typeof error.error === 'string' && error.error.trim()) ||
        error.error?.message ||
        error.message;

      if (backendMessage) {
        errorMessage = backendMessage;
      }
    }

    throw new Error(`${operation} - ${errorMessage}`);
  }




 //============================ADMIN GERE LES OFFRES============================

 // Gestion des offres - méthodes manquantes
getOfferStats(): Observable<any> {
  return this.http.get<any>(`${this.apiUrl}/offers/stats`).pipe(
    catchError(error => {
      console.error('Erreur stats offres:', error);
      return of(this.getMockOfferStats());
    })
  );
}

// ✅ MODIFIÉ : Activer une offre avec notification
  activateOffer(offerId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/offers/${offerId}/activate`, {}).pipe(
      tap(() => {
        // ✅ NOTIFICATION pour activation d'offre
        console.log('🟢 Offre activée, envoi notification...');
        
        const currentUser = this.authService.currentUserValue;
        if (currentUser) {
          this.businessEvents.notifySystemEvent(
            'OFFER_ACTIVATED',
            `Offre #${offerId} activée`,
            'SUCCESS',
            currentUser.id
          ).subscribe();
        }
      }),
      catchError(error => this.handleApiError('activation d\'offre', error))
    );
  }

deactivateOffer(offerId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/offers/${offerId}/deactivate`, {}).pipe(
      tap(() => {
        // ✅ NOTIFICATION pour désactivation d'offre
        console.log('🔴 Offre désactivée, envoi notification...');
        
        const currentUser = this.authService.currentUserValue;
        if (currentUser) {
          this.businessEvents.notifySystemEvent(
            'OFFER_DEACTIVATED',
            `Offre #${offerId} désactivée`,
            'WARNING',
            currentUser.id
          ).subscribe();
        }
      }),
      catchError(error => this.handleApiError('désactivation d\'offre', error))
    );
  }


completeBooking(bookingId: number): Observable<AdminBooking> {
  return this.http.post<any>(`${this.apiUrl}/bookings/${bookingId}/complete`, {}).pipe(
    map(booking => this.transformBooking(booking)),
    catchError(error => this.handleApiError('finalisation de la réservation', error))
  );
}


// ✅ NOUVELLE MÉTHODE : Helper pour déterminer la sévérité selon le statut
  private getSeverityForStatus(status: string): 'SUCCESS' | 'ERROR' | 'WARNING' | 'INFO' {
    switch (status.toUpperCase()) {
      case 'CONFIRMED':
      case 'COMPLETED':
      case 'ACTIVATED':
        return 'SUCCESS';
      case 'CANCELLED':
      case 'DELETED':
        return 'ERROR';
      case 'PENDING':
      case 'DEACTIVATED':
        return 'WARNING';
      default:
        return 'INFO';
    }
  }

// Données mockées pour les statistiques offres
private getMockOfferStats(): any {
  return {
    total: 28,
    pending: 12,
    confirmed: 10,
    cancelled: 6,
    confirmationRate: 62.5
  };

}

  
}
