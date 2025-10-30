import { Injectable } from '@angular/core';
import { UserStats } from '../models/UserStats.model';
import { HttpClient } from '@angular/common/http';
import { AuthService } from './auth.service';
import { Observable } from 'rxjs/internal/Observable';
import { map } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UserStatsService {

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}


   getUserStats(userId: number): Observable<UserStats> {
    return this.http.get<any[]>(`/api/reservations/user/${userId}`).pipe(
      map(bookings => this.calculateStats(bookings))
    );
  }

  private calculateStats(bookings: any[]): UserStats {
    const totalBookings = bookings.length;
    const activeBookings = bookings.filter(b => 
      b.status === 'PENDING' || b.status === 'CONFIRMED'
    ).length;
    const cancelledBookings = bookings.filter(b => 
      b.status === 'CANCELLED'
    ).length;
    const completedBookings = bookings.filter(b => 
      b.status === 'COMPLETED'
    ).length;

    // Calculer la date d'inscription (première réservation ou date actuelle)
    const memberSince = this.calculateMemberSince(bookings);

    return {
      totalBookings,
      activeBookings,
      cancelledBookings,
      completedBookings,
      memberSince
    };
  }


  private calculateMemberSince(bookings: any[]): string {
    if (bookings.length === 0) {
      return new Date().toLocaleDateString('fr-FR', { 
        year: 'numeric', 
        month: 'long' 
      });
    }
    const firstBooking = bookings.reduce((earliest, current) => {
      const earliestDate = new Date(earliest.createdAt || earliest.reservationDate);
      const currentDate = new Date(current.createdAt || current.reservationDate);
      return earliestDate < currentDate ? earliest : current;
    });

    const firstDate = new Date(firstBooking.createdAt || firstBooking.reservationDate);
    return firstDate.toLocaleDateString('fr-FR', { 
      year: 'numeric', 
      month: 'long' 
    });
  }

  
}
