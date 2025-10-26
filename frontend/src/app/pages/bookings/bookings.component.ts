import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { Observable } from 'rxjs';
import { BookingsService, Booking } from '../../core/services/bookings';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-bookings',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './bookings.component.html',
  styleUrl: './bookings.component.scss'
})
export class BookingsComponent implements OnInit {
  bookings: Booking[] = [];
  isLoading = false;
  errorMessage = '';

  // Variables pour les tests
  testResults: any;
  bookings$!: Observable<Booking[]>;

  constructor(
    private bookingsService: BookingsService,
    private authService: AuthService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.bookings$ = this.bookingsService.getMyBookings();
    this.loadBookings();
  }

  loadBookings(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.bookingsService.getMyBookings().subscribe({
      next: (bookings: Booking[]) => {
        this.bookings = bookings;
        this.isLoading = false;
        console.log('Bookings chargées:', bookings);
      },
      error: (error: any) => {
        this.errorMessage = 'Erreur lors du chargement des réservations';
        this.isLoading = false;
        console.error('Error loading bookings:', error);
      }
    });
  }

  createNewBooking(): void {
    this.router.navigate(['/offers']);
  }

  confirmBooking(reservationId: number): void {
    console.log('Confirmation de la réservation:', reservationId);
    
    this.bookingsService.confirmBooking(reservationId).subscribe({
      next: (updatedBooking: Booking) => {
        console.log('Réservation confirmée:', updatedBooking);
        this.loadBookings(); // Recharger la liste
      },
      error: (error: any) => {
        console.error('Error confirming booking:', error);
        this.errorMessage = error.error?.message || 'Erreur lors de la confirmation';
      }
    });
  }

  cancelBooking(reservationId: number): void {
    console.log('Annulation de la réservation:', reservationId);
    
    this.bookingsService.cancelBooking(reservationId).subscribe({
      next: (updatedBooking: Booking) => {
        console.log('Réservation annulée:', updatedBooking);
        this.loadBookings(); // Recharger la liste
      },
      error: (error: any) => {
        console.error('Error cancelling booking:', error);
        this.errorMessage = error.error?.message || 'Erreur lors de l\'annulation';
      }
    });
  }

  deleteBooking(reservationId: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette réservation ? Cette action est irréversible.')) {
      console.log('Suppression de la réservation:', reservationId);
      
      this.bookingsService.deleteBooking(reservationId).subscribe({
        next: () => {
          console.log('Réservation supprimée:', reservationId);
          // Supprimer de la liste localement au lieu de recharger
          this.bookings = this.bookings.filter(b => b.reservationId !== reservationId);
        },
        error: (error: any) => {
          console.error('Error deleting booking:', error);
          this.errorMessage = error.error?.message || 'Erreur lors de la suppression';
        }
      });
    }
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getStatusBadgeClass(status: string): string {
    switch (status?.toUpperCase()) {
      case 'CONFIRMED':
        return 'status-confirmed';
      case 'PENDING':
        return 'status-pending';
      case 'CANCELLED':
        return 'status-cancelled';
      case 'COMPLETED':
        return 'status-completed';
      default:
        return 'status-default';
    }
  }

  // TESTS DU BOOKING SERVICE
  testGetBookings(): void {
    console.log('🧪 Testing getBookings...');
    this.bookingsService.getMyBookings().subscribe({
      next: (data: Booking[]) => {
        this.testResults = { 
          method: 'getMyBookings', 
          status: 'SUCCESS', 
          data: data,
          timestamp: new Date()
        };
        console.log('✅ Bookings reçues:', data);
      },
      error: (error: any) => {
        this.testResults = { 
          method: 'getMyBookings', 
          status: 'ERROR', 
          error: error.message,
          timestamp: new Date()
        };
        console.error('❌ Erreur:', error);
      }
    });
  }
  
  testCreateBooking(): void {
    const mockBookingRequest = {
      userId: 1,
      offerId: Math.floor(Math.random() * 100) + 1,
      reservationDate: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString()
    };
    
    console.log('🧪 Creating booking:', mockBookingRequest);
    
    this.bookingsService.createBooking(mockBookingRequest).subscribe({
      next: (booking: Booking) => {
        this.testResults = { 
          method: 'createBooking', 
          status: 'SUCCESS', 
          data: booking,
          timestamp: new Date()
        };
        console.log('✅ Booking créé:', booking);
        this.loadBookings();
      },
      error: (error: any) => {
        this.testResults = { 
          method: 'createBooking', 
          status: 'ERROR', 
          error: error.message,
          timestamp: new Date()
        };
        console.error('❌ Erreur création:', error);
      }
    });
  }

  testConfirmBooking(): void {
    if (this.bookings.length > 0) {
      const bookingId = this.bookings[0].reservationId!;
      this.bookingsService.confirmBooking(bookingId).subscribe({
        next: (booking: Booking) => {
          this.testResults = { 
            method: 'confirmBooking', 
            status: 'SUCCESS', 
            data: booking,
            timestamp: new Date()
          };
          console.log('✅ Booking confirmé:', booking);
          this.loadBookings();
        },
        error: (error: any) => {
          this.testResults = { 
            method: 'confirmBooking', 
            status: 'ERROR', 
            error: error.message,
            timestamp: new Date()
          };
          console.error('❌ Erreur confirmation:', error);
        }
      });
    }
  }
}