import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { AdminStats } from '../models/AdminStats.model';
import { AdminUser } from '../models/AdminUser.model';
import { RecentActivity } from '../models/RecentActivity.model';
import { UserResponse } from '../models/UserResponse.model';
import { OfferResponse } from '../models/OfferReponse.model';
import { Offer, OfferStatus } from '../models/offer.model';
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

    return this.http.get<UserResponse>(`${this.apiUrl}/users`, { params }).pipe(
      map(response => ({
        ...response,
        content: response.content.map(user => this.transformUser(user))
      })),
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
   * Transforme un utilisateur backend en format frontend
   */
  transformUser(backendUser: any): AdminUser {
    return {
      id: backendUser.id,
      username: backendUser.username ?? backendUser.email?.split('@')[0] ?? 'Utilisateur',
      email: backendUser.email,
      role: backendUser.role?.name || backendUser.role || 'ROLE_USER',
      createdAt: backendUser.createdAt || backendUser.registrationDate || new Date().toISOString(),
      firstName: backendUser.firstName,
      lastName: backendUser.lastName,
      lastLogin: backendUser.lastLogin,
      status: backendUser.status || 'active',
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
    return {
      offerId: rawOffer.offerId ?? rawOffer.id,
      description: rawOffer.description ?? '',
      price: rawOffer.price ?? 0,
      pickupDatetime: rawOffer.pickupDatetime ?? rawOffer.pickupDate ?? new Date().toISOString(),
      createdAt: rawOffer.createdAt ?? new Date().toISOString(),
      updatedAt: rawOffer.updatedAt ?? rawOffer.createdAt ?? new Date().toISOString(),
      version: rawOffer.version ?? 0,
      adminId: rawOffer.adminId ?? rawOffer.admin?.id ?? 0,
      mobilityServiceId: rawOffer.mobilityServiceId ?? rawOffer.mobilityService?.id ?? 0,
      pickupLocationId: rawOffer.pickupLocationId ?? rawOffer.pickupLocation?.id ?? 0,
      returnLocationId: rawOffer.returnLocationId ?? rawOffer.returnLocation?.id ?? 0,
      status: this.normalizeOfferStatus(rawOffer.status),
      mobilityService: rawOffer.mobilityService?.name ?? rawOffer.mobilityServiceName,
      pickupLocation: rawOffer.pickupLocation?.name ?? rawOffer.pickupLocationName,
      returnLocation: rawOffer.returnLocation?.name ?? rawOffer.returnLocationName,
      adminName: rawOffer.admin?.username ?? rawOffer.adminName
    };
  }

  private normalizeOfferStatus(status?: string | null): OfferStatus {
    if (!status) {
      return 'ACTIVE';
    }
    const upper = status.toUpperCase();
    const allowed: OfferStatus[] = ['ACTIVE', 'INACTIVE', 'EXPIRED'];
    return allowed.includes(upper as OfferStatus) ? (upper as OfferStatus) : 'ACTIVE';
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
      catchError(error => {
        console.error('Erreur récupération réservation:', error);
        throw error;
      })
    );
  }

  /**
   * Met à jour le statut d'une réservation
   * PATCH /api/admin/bookings/{id}/status
   */
  updateBookingStatus(bookingId: number, newStatus: string): Observable<AdminBooking> {
    return this.http.patch<any>(`${this.apiUrl}/bookings/${bookingId}/status`, { status: newStatus }).pipe(
      map(booking => this.transformBooking(booking)),
      catchError(error => {
        console.error('Erreur mise à jour statut:', error);
        throw error;
      })
    );
  }

  /**
   * Annule une réservation
   * PATCH /api/admin/bookings/{id}/cancel
   */
  cancelBooking(bookingId: number): Observable<AdminBooking> {
    return this.http.patch<any>(`${this.apiUrl}/bookings/${bookingId}/cancel`, {}).pipe(
      map(booking => this.transformBooking(booking)),
      catchError(error => {
        console.error('Erreur annulation:', error);
        throw error;
      })
    );
  }

  /**
   * Supprime une réservation
   * DELETE /api/admin/bookings/{id}
   */
  deleteBooking(bookingId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/bookings/${bookingId}`).pipe(
      catchError(error => {
        console.error('Erreur suppression:', error);
        throw error;
      })
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
    return this.http.get<AdminUser>(`${this.apiUrl}/users/${userId}`).pipe(
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
    return this.http.put<AdminUser>(`${this.apiUrl}/users/${userId}`, userData).pipe(
      catchError(error => {
        console.error('Erreur mise à jour user:', error);
        throw error;
      })
    );
  }

  /**
   * Désactive un utilisateur
   * PATCH /api/admin/users/{id}/deactivate
   */
  deactivateUser(userId: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/users/${userId}/deactivate`, {}).pipe(
      catchError(error => {
        console.error('Erreur désactivation user:', error);
        throw error;
      })
    );
  }

  /**
   * Réactive un utilisateur
   * PATCH /api/admin/users/{id}/activate
   */
  activateUser(userId: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/users/${userId}/activate`, {}).pipe(
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
    return this.http.delete<void>(`${this.apiUrl}/users/${userId}`).pipe(
      catchError(error => {
        console.error('Erreur suppression user:', error);
        throw error;
      })
    );
  }

  /**
   * Change le rôle d'un utilisateur
   * PATCH /api/admin/users/{id}/role
   */
  changeUserRole(userId: number, newRole: string): Observable<AdminUser> {
    return this.http.patch<AdminUser>(`${this.apiUrl}/users/${userId}/role`, { role: newRole }).pipe(
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

    return this.http.get<UserResponse>(`${this.apiUrl}/users/search`, { params }).pipe(
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
      catchError(error => {
        console.error('Erreur récupération offre:', error);
        throw error;
      })
    );
  }

  /**
   * Crée une nouvelle offre
   * POST /api/admin/offers
   */
  createOffer(offerData: any): Observable<Offer> {
    return this.http.post<Offer>(`${this.apiUrl}/offers`, offerData).pipe(
      catchError(error => {
        console.error('Erreur création offre:', error);
        throw error;
      })
    );
  }


   /**
   * Met à jour une offre
   * PUT /api/admin/offers/{id}
   */
  updateOffer(offerId: number, offerData: any): Observable<Offer> {
    return this.http.put<Offer>(`${this.apiUrl}/offers/${offerId}`, offerData).pipe(
      catchError(error => {
        console.error('Erreur mise à jour offre:', error);
        throw error;
      })
    );
  }


  /**
   * Supprime une offre
   * DELETE /api/admin/offers/{id}
   */
  deleteOffer(offerId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/offers/${offerId}`).pipe(
      catchError(error => {
        console.error('Erreur suppression offre:', error);
        throw error;
      })
    );
  }

   /**
   * Change le statut d'une offre
   * PATCH /api/admin/offers/{id}/status
   */
  changeOfferStatus(offerId: number, newStatus: string): Observable<Offer> {
    return this.http.patch<Offer>(`${this.apiUrl}/offers/${offerId}/status`, { status: newStatus }).pipe(
      catchError(error => {
        console.error('Erreur changement statut:', error);
        throw error;
      })
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
        adminId: 1,
        mobilityServiceId: 1,
        pickupLocationId: 1,
        returnLocationId: 1,
        status: 'ACTIVE',
        mobilityService: 'Voiture électrique',
        pickupLocation: 'Paris Centre',
        returnLocation: 'Paris Centre',
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
        adminId: 1,
        mobilityServiceId: 2,
        pickupLocationId: 2,
        returnLocationId: 3,
        status: 'ACTIVE',
        mobilityService: 'Scooter électrique',
        pickupLocation: 'Lyon Part-Dieu',
        returnLocation: 'Lyon Perrache',
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
        adminId: 2,
        mobilityServiceId: 3,
        pickupLocationId: 3,
        returnLocationId: 3,
        status: 'INACTIVE',
        mobilityService: 'Vélo électrique',
        pickupLocation: 'Marseille Vieux-Port',
        returnLocation: 'Marseille Vieux-Port',
        adminName: 'Admin System'
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


}

  
