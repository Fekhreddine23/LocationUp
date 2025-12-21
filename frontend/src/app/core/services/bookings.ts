import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { delay, tap, catchError } from 'rxjs/operators';
import { AuthService } from './auth.service';
import { LoadingService } from './loading.service';
import { BusinessEventsService } from './business-events/business-events';
import { DriverProfile } from '../models/driver-profile.model';

export interface Booking {
  reservationId?: number;
  userId: number;
  offerId: number;
  reservationDate: string;
  status: 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED';
  createdAt?: string;
  updatedAt?: string;
  paymentStatus?: 'PENDING' | 'REQUIRES_ACTION' | 'PAID' | 'FAILED' | 'REFUNDED' | 'EXPIRED';
  paymentReference?: string;
  paymentDate?: string;
  driverProfile?: DriverProfile;
}

export interface BookingTimelineEvent {
  title: string;
  description: string;
  status: string;
  timestamp: string;
}

export interface BookingTimeline {
  reservationId: number;
  status?: string;
  paymentStatus?: string;
  events: BookingTimelineEvent[];
}

export interface CreateBookingRequest {
  userId: number;
  offerId: number;
  reservationDate: string;
  driverProfile?: DriverProfile;
}

@Injectable({
  providedIn: 'root'
})
export class BookingsService {
  private apiUrl = 'http://localhost:8088/api/reservations';
  private useMockData = false;

  private mockBookings: Booking[] = [
    {
      reservationId: 1,
      userId: 1,
      offerId: 101,
      reservationDate: '2025-10-26T10:00:00',
      status: 'PENDING',
      createdAt: '2025-10-25T09:00:00',
      updatedAt: '2025-10-25T09:00:00'
    },
    {
      reservationId: 2,
      userId: 1,
      offerId: 102,
      reservationDate: '2025-10-27T14:30:00',
      status: 'CONFIRMED',
      createdAt: '2025-10-24T16:20:00',
      updatedAt: '2025-10-25T08:15:00'
    }
  ];

  constructor(
    private http: HttpClient,
    private authService: AuthService,
    private loadingService: LoadingService,
    private businessEvents: BusinessEventsService 
  ) {}

  private getHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  // Simuler un délai réseau pour les mocks
  private simulateDelay<T>() {
    return delay<T>(1000 + Math.random() * 1000);
  }

  // Méthode générique pour gérer le loading
  private withLoading<T>(key: string, observable: Observable<T>): Observable<T> {
    this.loadingService.setLoading(key, true);
    return observable.pipe(
      tap(() => this.loadingService.setLoading(key, false)),
      catchError(error => {
        this.loadingService.setLoading(key, false);
        return throwError(() => error);
      })
    );
  }

  // Récupérer toutes les réservations
  getAllBookings(): Observable<Booking[]> {
    if (this.useMockData) {
      return of([...this.mockBookings]).pipe(this.simulateDelay());
    }
    return this.withLoading('all-bookings', 
      this.http.get<Booking[]>(this.apiUrl, { headers: this.getHeaders() })
    );
  }

  // Récupérer les réservations d'un utilisateur
  getBookingsByUser(userId: number): Observable<Booking[]> {
    if (this.useMockData) {
      const userBookings = this.mockBookings.filter(booking => booking.userId === userId);
      return of(userBookings).pipe(this.simulateDelay());
    }
    return this.withLoading(`user-${userId}-bookings`,
      this.http.get<Booking[]>(`${this.apiUrl}/user/${userId}`, { headers: this.getHeaders() })
    );
  }

  // Récupérer mes réservations (utilisateur connecté)
  getMyBookings(): Observable<Booking[]> {
    const currentUser = this.authService.currentUserValue;
    if (!currentUser) {
      throw new Error('Utilisateur non connecté');
    }

    return this.getBookingsByUser(currentUser.id);
  }

  // Récupérer une réservation par ID
  getBookingById(id: number): Observable<Booking> {
    if (this.useMockData) {
      const booking = this.mockBookings.find(b => b.reservationId === id);
      if (booking) {
        return of(booking).pipe(this.simulateDelay());
      }
      throw new Error('Booking not found');
    }
    return this.withLoading(`booking-${id}`,
      this.http.get<Booking>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() })
    );
  }

  getTimeline(reservationId: number): Observable<BookingTimeline> {
    return this.withLoading(`timeline-${reservationId}`,
      this.http.get<BookingTimeline>(`${this.apiUrl}/${reservationId}/timeline`, { headers: this.getHeaders() })
    );
  }

  downloadReceipt(reservationId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${reservationId}/receipt`, {
      headers: this.getHeaders(),
      responseType: 'blob'
    });
  }

  // Créer une nouvelle réservation
  createBooking(bookingData: CreateBookingRequest): Observable<Booking> {
    if (this.useMockData) {
      const newBooking: Booking = {
        reservationId: Math.max(...this.mockBookings.map(b => b.reservationId!)) + 1,
        ...bookingData,
        status: 'PENDING',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };
      
      this.mockBookings.push(newBooking);
      // ✅ NOTIFICATION pour création
      this.businessEvents.notifyBookingCreated(
        newBooking.reservationId!,
        newBooking.userId,
        { message: 'Réservation créée avec succès' }
      ).subscribe();
      
      return of(newBooking).pipe(this.simulateDelay());
    }
    return this.withLoading('create-booking',
      this.http.post<Booking>(this.apiUrl, bookingData, { headers: this.getHeaders() }).pipe(
        tap((newBooking: Booking) => {
          // ✅ NOTIFICATION pour création réelle
          console.log('🎉 Réservation créée, envoi notification...');
          this.businessEvents.notifyBookingCreated(
            newBooking.reservationId!,
            newBooking.userId,
            { message: 'Votre réservation a été créée' }
          ).subscribe({
            next: () => console.log('✅ Notification réservation envoyée'),
            error: (err) => console.error('❌ Erreur notification:', err)
          });
        })
      )
    );
  }

  // Mettre à jour une réservation
  updateBooking(bookingId: number, bookingData: Partial<Booking>): Observable<Booking> {
    if (this.useMockData) {
      const booking = this.mockBookings.find(b => b.reservationId === bookingId);
      if (booking) {
        Object.assign(booking, bookingData);
        booking.updatedAt = new Date().toISOString();
        return of(booking).pipe(this.simulateDelay());
      }
      throw new Error('Booking not found');
    }
    return this.withLoading(`update-${bookingId}`,
      this.http.put<Booking>(`${this.apiUrl}/${bookingId}`, bookingData, { headers: this.getHeaders() })
    );
  }

   // ✅ MODIFIÉ : Confirmer une réservation avec notification
  confirmBooking(bookingId: number): Observable<Booking> {
    if (this.useMockData) {
      const booking = this.mockBookings.find(b => b.reservationId === bookingId);
      if (booking) {
        booking.status = 'CONFIRMED';
        booking.updatedAt = new Date().toISOString();
        
        // ✅ NOTIFICATION pour confirmation
        this.businessEvents.notifyBookingConfirmed(
          bookingId,
          booking.userId
        ).subscribe();
        
        return of(booking).pipe(this.simulateDelay());
      }
      throw new Error('Booking not found');
    }

    return this.withLoading(`confirm-${bookingId}`,
      this.http.patch<Booking>(`${this.apiUrl}/${bookingId}/confirm`, {}, { headers: this.getHeaders() }).pipe(
        tap((booking: Booking) => {
          // ✅ NOTIFICATION pour confirmation réelle
          this.businessEvents.notifyBookingConfirmed(
            bookingId,
            booking.userId
          ).subscribe();
        })
      )
    );
  }

  // ✅ MODIFIÉ : Annuler une réservation avec notification
  cancelBooking(bookingId: number): Observable<Booking> {
    if (this.useMockData) {
      const booking = this.mockBookings.find(b => b.reservationId === bookingId);
      if (booking) {
        booking.status = 'CANCELLED';
        booking.updatedAt = new Date().toISOString();
        
        // ✅ NOTIFICATION pour annulation
        this.businessEvents.notifyBookingCancelled(
          bookingId,
          booking.userId,
          'Annulé par l\'utilisateur'
        ).subscribe();
        
        return of(booking).pipe(this.simulateDelay());
      }
      throw new Error('Booking not found');
    }

    return this.withLoading(`cancel-${bookingId}`,
      this.http.patch<Booking>(`${this.apiUrl}/${bookingId}/cancel`, {}, { headers: this.getHeaders() }).pipe(
        tap((booking: Booking) => {
          // ✅ NOTIFICATION pour annulation réelle
          this.businessEvents.notifyBookingCancelled(
            bookingId,
            booking.userId,
            'Réservation annulée'
          ).subscribe();
        })
      )
    );
  }

  // Supprimer une réservation
  deleteBooking(bookingId: number): Observable<void> {
    if (this.useMockData) {
      this.mockBookings = this.mockBookings.filter(b => b.reservationId !== bookingId);
      return of(void 0).pipe(this.simulateDelay());
    }
    return this.withLoading(`delete-${bookingId}`,
      this.http.delete<void>(`${this.apiUrl}/${bookingId}`, { headers: this.getHeaders() })
    );
  }

  // Méthode pour basculer entre mock et vrai backend
  setUseMockData(useMock: boolean): void {
    this.useMockData = useMock;
  }
}
