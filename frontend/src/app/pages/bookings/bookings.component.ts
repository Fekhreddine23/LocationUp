import { Router, RouterModule } from "@angular/router";
import { OffersService } from "../../core/services/offers.service";
import { Observable } from "rxjs/internal/Observable";
import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { AuthService } from "../../core/services/auth.service";
import { ConfirmationModal } from "../../components/confirmation-modal/confirmation-modal";
import { DeleteConfirmationModal } from "../../components/delete-confirmation-modal/delete-confirmation-modal";
import { BookingsService, Booking, BookingTimeline } from '../../core/services/bookings'; // ← Utiliser Booking du service
import { NotificationService } from "../../core/services/notification.service";
import { LoadingService } from "../../core/services/loading.service";
import { Spinner } from "../../components/spinner/spinner";
import { BreadcrumbService } from "../../core/services/breadcrumb";
import { Breadcrumbs } from "../../components/breadcrumbs/breadcrumbs";
import { PaymentService } from '../../core/services/payment.service';



@Component({
  selector: 'app-bookings',
  standalone: true,
  imports: [CommonModule, RouterModule, ConfirmationModal, DeleteConfirmationModal, Spinner, Breadcrumbs], // ← AJOUT
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
  timelines: { [reservationId: number]: BookingTimeline } = {};
  timelineVisibility: { [reservationId: number]: boolean } = {};
  timelineLoading: { [reservationId: number]: boolean } = {};
  paymentSyncLoading: { [reservationId: number]: boolean } = {};


  constructor(
    private bookingsService: BookingsService,
    private authService: AuthService,
    private offersService: OffersService,
    private router: Router,
    private notificationService: NotificationService,
    private loadingService: LoadingService,
    private paymentService: PaymentService,

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
    })

  }

  breadcrumbItems = [
    { label: 'Tableau de Bord', url: '/dashboard' }, // ← Changer "Accueil" par "Dashboard"
    { label: 'Mes Réservations', url: '/bookings', active: true }
  ];

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

  getPaymentStatusText(status?: string): string {
    if (!status) {
      return 'Paiement en attente';
    }
    const map: { [key: string]: string } = {
      PENDING: 'Paiement en attente',
      REQUIRES_ACTION: 'Action requise',
      PAID: 'Payé',
      FAILED: 'Échec du paiement',
      REFUNDED: 'Remboursé'
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
      default:
        return 'payment-pending';
    }
  }

  syncPayment(booking: Booking): void {
    if (!booking.reservationId) {
      return;
    }
    const reservationId = booking.reservationId;
    this.paymentSyncLoading[reservationId] = true;
    this.paymentService.syncPaymentStatus(reservationId).subscribe({
      next: (response) => {
        this.paymentSyncLoading[reservationId] = false;
        const status = (response.paymentStatus || booking.paymentStatus || '').toUpperCase();

        if (status === 'PAID' || response.updated) {
          this.notificationService.success('Paiement synchronisé avec succès', 4000);
          this.loadBookings();
          return;
        }

        const stripeStatus = response.stripeStatus?.toLowerCase();
        if (status === 'REQUIRES_ACTION' || stripeStatus === 'unpaid') {
          this.notificationService.success('Redirection vers Stripe pour finaliser le paiement', 4000);
          this.paymentService.startCheckout(reservationId).catch((err) => {
            console.error('Relance paiement échouée', err);
            this.notificationService.error('Impossible de relancer le paiement pour le moment', 5000);
          });
          return;
        }

        if (status === 'FAILED') {
          this.notificationService.error('Le paiement est expiré/échoué. Relancez depuis une nouvelle réservation.', 5000);
          this.loadBookings();
          return;
        }

        this.notificationService.success(`Paiement synchronisé (${status || 'UNKNOWN'})`, 4000);
        this.loadBookings();
      },
      error: (err) => {
        this.paymentSyncLoading[reservationId] = false;
        console.error('Sync paiement échoué', err);
        this.notificationService.error('Impossible de vérifier le paiement', 5000);
      }
    });
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

  toggleTimeline(booking: Booking): void {
    if (!booking.reservationId) {
      return;
    }
    this.timelineVisibility[booking.reservationId] = !this.timelineVisibility[booking.reservationId];
    if (this.timelineVisibility[booking.reservationId] && !this.timelines[booking.reservationId]) {
      this.loadTimeline(booking.reservationId);
    }
  }

  private loadTimeline(reservationId: number): void {
    this.timelineLoading[reservationId] = true;
    this.bookingsService.getTimeline(reservationId).subscribe({
      next: (timeline) => {
        this.timelines[reservationId] = timeline;
        this.timelineLoading[reservationId] = false;
      },
      error: () => {
        this.timelineLoading[reservationId] = false;
        this.notificationService.error('Impossible de charger la timeline', 4000);
      }
    });
  }

  downloadReceipt(reservationId: number): void {
    this.bookingsService.downloadReceipt(reservationId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `reservation-${reservationId}-receipt.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => this.notificationService.error('Reçu indisponible pour le moment', 4000)
    });
  }

  getTimelineEvents(booking: Booking): BookingTimeline | null {
    if (!booking.reservationId) {
      return null;
    }
    return this.timelines[booking.reservationId] || null;
  }

  getTimelineStatusClass(status: string): string {
    switch (status?.toUpperCase()) {
      case 'PAID':
      case 'COMPLETED':
      case 'CONFIRMED':
        return 'timeline-status-success';
      case 'PAYMENT_FAILED':
      case 'FAILED':
      case 'CANCELLED':
        return 'timeline-status-danger';
      case 'PAYMENT_PENDING':
      case 'PENDING':
        return 'timeline-status-warning';
      default:
        return 'timeline-status-default';
    }
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

  formatDate(dateString?: string): string {
    if (!dateString) {
      return '—';
    }
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
