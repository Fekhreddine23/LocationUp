import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { AdminStats } from '../models/AdminStats.model';
import { AdminUser } from '../models/AdminUser.model';
import { RecentActivity } from '../models/RecentActivity.model';
import { UserResponse } from '../models/UserResponse.model';
import { OfferResponse } from '../models/OfferReponse.model';
import { CreateOfferRequest, Offer, OfferStatus } from '../models/offer.model';
import { AdminBooking, BookingResponse } from '../models/AdminBooking.model';




@Injectable({
  providedIn: 'root'
})
export class AdminService {

    constructor(private http: HttpClient) {}


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
    const content = Array.isArray(response?.content) ? response.content : [];
    return {
      content: content.map((booking: any) => this.transformBooking(booking)),
      totalElements: response?.totalElements ?? content.length ?? 0,
      totalPages: response?.totalPages ?? 1,
      size: response?.size ?? content.length ?? 0,
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

    return {
      reservationId: rawBooking.reservationId ?? rawBooking.id ?? 0,
      reservationDate: rawBooking.reservationDate ?? rawBooking.date ?? new Date().toISOString(),
      status: this.normalizeBookingStatus(rawBooking.status),
      createdAt: rawBooking.createdAt ?? rawBooking.created_at,
      totalPrice: rawBooking.totalPrice ?? rawBooking.price,
      user: rawBooking.user
        ? {
            id: rawBooking.user.id,
            username: rawBooking.user.username,
            email: rawBooking.user.email
          }
        : undefined,
      offer: rawBooking.offer
        ? {
            offerId: rawBooking.offer.offerId ?? rawBooking.offer.id ?? 0,
            mobilityService: rawBooking.offer.mobilityService ?? rawBooking.offer.serviceName,
            pickupLocation: rawBooking.offer.pickupLocation,
            price: rawBooking.offer.price
          }
        : undefined
    };
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

  private transformOffer(rawOffer: any): Offer {
    const pickupLocationName = rawOffer.pickupLocation?.name ?? rawOffer.pickupLocationName ?? rawOffer.pickupLocation ?? '';
    const returnLocationName = rawOffer.returnLocation?.name ?? rawOffer.returnLocationName ?? rawOffer.returnLocation ?? '';
    const mobilityServiceName = rawOffer.mobilityService?.name ?? rawOffer.mobilityServiceName ?? rawOffer.mobilityService ?? '';

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
      averageBookingValue: 80.39
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
   catchError(error => this.handleApiError('récupération de l\'offre', error))
      
    );
  }

  /**
   * Crée une nouvelle offre
   * POST /api/admin/offers
   */
  createOffer(offerData: CreateOfferRequest): Observable<Offer> {
    return this.http.post<Offer>(`${this.apiUrl}/offers`, offerData).pipe(
      catchError(error => this.handleApiError('récupération de l\'offre', error))
    );
  }


   /**
   * Met à jour une offre
   * PUT /api/admin/offers/{id}
   */
  updateOffer(offerId: number, offerData: any): Observable<Offer> {
    const payload = this.buildOfferUpdatePayload(offerData);
    return this.http.put<Offer>(`${this.apiUrl}/offers/${offerId}`, payload).pipe(
      map(response => this.transformOffer(response)),
      catchError(error => this.handleApiError('mise à jour de l\'offre', error))
    );
  }


  /**
   * Supprime une offre
   * DELETE /api/admin/offers/{id}
   */
  deleteOffer(offerId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/offers/${offerId}`).pipe(
      catchError(error => this.handleApiError('récupération de l\'offre', error))
    );
  }

   /**
   * Change le statut d'une offre
   * POST /api/admin/offers/{id}/status
   */
  changeOfferStatus(offerId: number, newStatus: string): Observable<Offer> {
    return this.http.post<Offer>(`${this.apiUrl}/offers/${offerId}/status`, { status: newStatus }).pipe(
     catchError(error => this.handleApiError('récupération de l\'offre', error))
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
  } else if (error.error?.message) {
    errorMessage = error.error.message;
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

activateOffer(offerId: number): Observable<void> {
  return this.http.post<void>(`${this.apiUrl}/offers/${offerId}/activate`, {}).pipe(
    catchError(error => this.handleApiError('activation d\'offre', error))
  );
}

deactivateOffer(offerId: number): Observable<void> {
  return this.http.post<void>(`${this.apiUrl}/offers/${offerId}/deactivate`, {}).pipe(
    catchError(error => this.handleApiError('désactivation d\'offre', error))
  );
}

completeBooking(bookingId: number): Observable<AdminBooking> {
  return this.http.post<any>(`${this.apiUrl}/bookings/${bookingId}/complete`, {}).pipe(
    map(booking => this.transformBooking(booking)),
    catchError(error => this.handleApiError('finalisation de la réservation', error))
  );
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
