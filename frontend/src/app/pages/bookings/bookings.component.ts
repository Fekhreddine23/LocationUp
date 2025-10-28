import { Router, RouterModule } from "@angular/router";
import { ReservationDTO } from "../../core/models/reservation.model";
import { OffersService } from "../../core/services/offers.service";
import { Observable } from "rxjs/internal/Observable";
import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { AuthService } from "../../core/services/auth.service";
import { ConfirmationModal } from "../../components/confirmation-modal/confirmation-modal";
import { DeleteConfirmationModal } from "../../components/delete-confirmation-modal/delete-confirmation-modal";
import { BookingsService, Booking } from '../../core/services/bookings'; // ← Utiliser Booking du service
import { NotificationService } from "../../core/services/notification.service";
import { LoadingService } from "../../core/services/loading.service";
import { Spinner } from "../../components/spinner/spinner";



@Component({
  selector: 'app-bookings',
  standalone: true,
  imports: [CommonModule, RouterModule, ConfirmationModal, DeleteConfirmationModal, Spinner], // ← AJOUT
  templateUrl: './bookings.component.html',
  styleUrl: './bookings.component.scss'
})
export class BookingsComponent implements OnInit {
  bookings: Booking[] = [];
  isLoading = false;
  errorMessage = '';


  // NOUVEAU : États de loading spécifiques
  loadingStates: { [key: string]: boolean } = {};

  // Pour stocker les détails des offres
  offerDetails: { [offerId: number]: any } = {};
  deleteModalGetOfferDetails = (offerId: number) => this.getOfferDetails(offerId);

  // Variables pour les tests
  testResults: any;
  bookings$!: Observable<Booking[]>; // ← CHANGER LE TYPE

  // NOUVELLES VARIABLES POUR LE MODAL
  showCancelModal = false;
  selectedBooking: Booking | null = null;
  isCancelling = false;
  showCancelReason = true;

  // Variables pour le modal de suppression
  showDeleteModal = false;
  bookingToDelete: Booking | null = null;

  constructor(
    private bookingsService: BookingsService,
    private authService: AuthService,
    private offersService: OffersService,
    private router: Router,
    private notificationService: NotificationService,
    private loadingService: LoadingService 
  ) { }

  ngOnInit(): void {
    this.bookings$ = this.bookingsService.getMyBookings();
    this.loadBookings();


    // NOUVEAU : Écouter les états de loading globaux
    this.loadingService.loading$.subscribe(loading => {
      this.isLoading = loading;
    });

    // NOUVEAU : Écouter les états de loading spécifiques aux réservations
    this.loadingService.getLoading('bookings').subscribe(loading => {
      this.loadingStates['bookings'] = loading;
    });

  }

  loadBookings(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.bookingsService.getMyBookings().subscribe({
      next: (bookings: Booking[]) => { // ← CHANGER LE TYPE
        this.bookings = bookings;
        this.isLoading = false;
        console.log('Bookings chargées:', bookings);
        // Charger les détails des offres pour chaque réservation
        this.loadOfferDetails(bookings);
      },
      error: (error: any) => {
        this.errorMessage = 'Erreur lors du chargement des réservations';
        this.isLoading = false;
        console.error('Error loading bookings:', error);
      }
    });
  }

   


  // NOUVELLE MÉTHODE : Vérifier si une action est en cours de chargement
  isLoadingAction(bookingId: number, action: string): boolean {
    return this.loadingStates[`${action}-${bookingId}`] || false;
  }

   // NOUVELLE MÉTHODE : Écouter le loading d'une action spécifique
  private setupActionLoading(bookingId: number, action: string): void {
    this.loadingService.getLoading(`${action}-${bookingId}`).subscribe(loading => {
      this.loadingStates[`${action}-${bookingId}`] = loading;
    });
  }



  // NOUVELLE MÉTHODE : Charger les détails des offres
  private loadOfferDetails(bookings: Booking[]): void {
    const uniqueOfferIds = [...new Set(bookings.map(b => b.offerId))];

    uniqueOfferIds.forEach(offerId => {
      this.offersService.getOfferById(offerId).subscribe({
        next: (offer: any) => {
          this.offerDetails[offerId] = offer;
        },
        error: (error: any) => {
          console.error(`Error loading offer ${offerId}:`, error);
        }
      });
    });
  }

  // NOUVELLE MÉTHODE : Obtenir les détails d'une offre
  getOfferDetails(offerId: number): any {
    return this.offerDetails[offerId] || null;
  }

  // NOUVELLES MÉTHODES POUR L'AFFICHAGE
  getCountByStatus(status: string): number {
    return this.bookings.filter(booking => booking.status === status).length;
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

  getStatusCardClass(status: string): string {
    switch (status?.toUpperCase()) {
      case 'CONFIRMED':
        return 'booking-card-confirmed';
      case 'PENDING':
        return 'booking-card-pending';
      case 'CANCELLED':
        return 'booking-card-cancelled';
      case 'COMPLETED':
        return 'booking-card-completed';
      default:
        return 'booking-card-default';
    }
  }

  createNewBooking(): void {
    this.router.navigate(['/offers']);
  }

  confirmBooking(reservationId: number): void {
    console.log('Confirmation de la réservation:', reservationId);

    this.bookingsService.confirmBooking(reservationId).subscribe({
      next: (updatedBooking: Booking) => { // ← CHANGER LE TYPE
        console.log('Réservation confirmée:', updatedBooking);
        this.loadBookings(); // Recharger la liste
        this.notificationService.success('✅ Réservation confirmée avec succès', 4000);

      },
      error: (error: any) => {
        console.error('Error confirming booking:', error);
        this.notificationService.error('❌ Erreur lors de la confirmation', 5000)
      }
    });
  }

  // SUPPRIMER L'ANCIENNE MÉTHODE cancelBooking (celle en commentaire)

  // NOUVELLE MÉTHODE : Ouvrir le modal d'annulation
  openCancelModal(booking: Booking): void {
    this.selectedBooking = booking;
    this.showCancelModal = true;
  }

  onCancelConfirmed(event: { reason?: string }): void {
    if (this.selectedBooking?.reservationId) {
      this.isCancelling = true;
      const reservationId = this.selectedBooking.reservationId;
      // NOUVEAU : Configurer l'écoute du loading pour l'annulation
      this.setupActionLoading(reservationId, 'cancel');

      console.log('Annulation avec raison:', event.reason);

      this.bookingsService.cancelBooking(this.selectedBooking.reservationId).subscribe({
        next: (updatedBooking: Booking) => {
          console.log('Réservation annulée:', updatedBooking);
          this.isCancelling = false;
          this.showCancelModal = false;
          this.selectedBooking = null;

          // Recharger la liste
          this.loadBookings();

          // Afficher un message de succès
          // ⬇️ TOAST D'ANNULATION
          this.notificationService.success('✅ Réservation annulée avec succès', 4000);
        },
        error: (error: any) => {
          console.error('Error cancelling booking:', error);
          this.isCancelling = false;
          // ⬇️ TOAST D'ERREUR D'ANNULATION
          this.notificationService.error('❌ Erreur lors de l\'annulation', 5000);
        }
      });
    }
  }

  onCancelCancelled(): void {
    this.showCancelModal = false;
    this.selectedBooking = null;
    this.isCancelling = false;
  }

   

  // SUPPRIMER 
  deleteBooking(reservationId: number): void {
     if (confirm('Êtes-vous sûr de vouloir supprimer cette réservation ? Cette action est irréversible.')) {
      console.log('Suppression de la réservation:', reservationId);

      // NOUVEAU : Configurer l'écoute du loading pour la suppression
      this.setupActionLoading(reservationId, 'delete');

      this.bookingsService.deleteBooking(reservationId).subscribe({
        next: () => {
          console.log('Réservation supprimée:', reservationId);
          // Supprimer de la liste localement au lieu de recharger
          this.bookings = this.bookings.filter(b => b.reservationId !== reservationId);
          this.notificationService.success('🗑️ Réservation supprimée avec succès', 4000);
        },
        error: (error: any) => {
          console.error('Error deleting booking:', error);
          this.notificationService.error('❌ Erreur lors de la suppression', 5000);
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

  // TESTS DU BOOKING SERVICE - CORRIGER LES TYPES
  testGetBookings(): void {
    console.log('🧪 Testing getBookings...');
    this.bookingsService.getMyBookings().subscribe({
      next: (data: Booking[]) => { // ← CHANGER LE TYPE
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
      next: (booking: Booking) => { // ← CHANGER LE TYPE
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
        next: (booking: Booking) => { // ← CHANGER LE TYPE
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

  // Méthode pour ouvrir le modal de suppression
  openDeleteModal(booking: Booking): void {
    this.bookingToDelete = booking;
    this.showDeleteModal = true;
  }

  // Méthode pour confirmer la suppression
  onDeleteConfirmed(): void {
    if (this.bookingToDelete?.reservationId) {
      this.deleteBooking(this.bookingToDelete.reservationId);
      this.showDeleteModal = false;
      this.bookingToDelete = null;
    }
  }

  // Méthode pour annuler la suppression
  onDeleteCancelled(): void {
    this.showDeleteModal = false;
    this.bookingToDelete = null;
  }

}
