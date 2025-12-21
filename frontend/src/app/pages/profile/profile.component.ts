import { Component, OnInit } from '@angular/core';
import { Breadcrumbs } from '../../components/breadcrumbs/breadcrumbs';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { User } from '../../core/models/auth.models';
import { UserStatsService } from '../../core/services/user-stats.service';
import { TwoFactorSetup } from '../../components/twoFactor/two-factor-setup';
import { IdentityService } from '../../core/services/identity.service';
import { IdentityStatus } from '../../core/models/identity.model';
import { NotificationService } from '../../core/services/notification.service';
import { loadStripe, Stripe } from '@stripe/stripe-js';
import { environment } from '../../../environments/environment';
import { firstValueFrom } from 'rxjs';
import { DriverProfile, createEmptyDriverProfile } from '../../core/models/driver-profile.model';
import { DriverProfileService } from '../../core/services/driver-profile.service';
import { BookingsService, Booking } from '../../core/services/bookings';
import { PaymentService } from '../../core/services/payment.service';
import { ProfileService } from '../../core/services/profile.service';

@Component({
  selector: 'app-profile',
    imports: [CommonModule, RouterModule, FormsModule, Breadcrumbs, TwoFactorSetup],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss'
})
export class ProfileComponent implements OnInit {

  user: User | null = null;
  isLoading = false;
  errorMessage = '';
  isEditing = false; // ← Mode édition

  //Données d'édition profil
  editForm = {
    username: '',
    email: ''
  };
  
  breadcrumbItems = [
    { label: 'Tableau de Bord', url: '/dashboard' },
    { label: 'Mon Profil', url: '/profile', active: true }
  ];

  userStats = {
    totalBookings: 0,
    activeBookings: 0,
    cancelledBookings: 0,
    completedBookings: 0
  };
  identityStatus: IdentityStatus | null = null;
  identityLoading = false;
  identityActionLoading = false;
  identityError = '';
  driverProfile: DriverProfile = createEmptyDriverProfile();
  driverProfileLoading = false;
  driverProfileSaving = false;
  driverProfileMessage = '';
  isAdmin = false;
  bookingHistory: Booking[] = [];
  bookingHistoryLoading = false;
  bookingHistoryError = '';
  avatarPreview: string | null = null;
  avatarMessage = '';
  mobilityPreferences = {
    favoriteCategory: 'Voiture',
    maxBudget: 80,
    ecoFriendlyOnly: false,
    autopilotNotifications: true
  };
  securityPreferences = {
    emailAlerts: true,
    smsAlerts: false,
    suspiciousLoginAlerts: true
  };
  private stripePromise: Promise<Stripe | null>;

  constructor(
    private authService: AuthService,
    private userStatsService: UserStatsService,
    private router: Router,
    private identityService: IdentityService,
    private notificationService: NotificationService,
    private driverProfileService: DriverProfileService,
    private bookingsService: BookingsService,
    private paymentService: PaymentService,
    private profileService: ProfileService
  ) {
    this.stripePromise = loadStripe(environment.stripe.publishableKey);
  }

  ngOnInit(): void {
    this.loadUserProfile();
  }

  loadUserProfile(): void {
    this.isLoading = true;
    this.errorMessage = '';

    // Récupérer l'utilisateur connecté
    this.user = this.authService.currentUserValue;
    this.avatarPreview = this.user?.avatarUrl ?? null;
    this.isAdmin = (this.user?.role === 'ROLE_ADMIN');
    
    if (!this.user) {
      this.errorMessage = 'Utilisateur non connecté';
      this.isLoading = false;
      return;
    }
    // Charger les vraies statistiques
    this.userStatsService.getUserStats(this.user.id).subscribe({
      next: (stats) => {
        this.userStats = stats;
        this.isLoading = false;
        this.fetchIdentityStatus();
        this.loadDriverProfile();
        this.loadBookingHistory();
      },
      error: (error) => {
        console.error('Erreur chargement statistiques:', error);
        this.errorMessage = 'Erreur lors du chargement des statistiques';
        this.isLoading = false;
        this.fetchIdentityStatus();
        this.loadDriverProfile();
        this.loadBookingHistory();
      }
    });
    this.loadLocalPreferences();
    this.loadAvatarFromServer();
  }

  fetchIdentityStatus(): void {
    if (!this.user) {
      return;
    }
    this.identityLoading = true;
    this.identityError = '';
    this.identityService.getStatus().subscribe({
      next: (status) => {
        this.identityStatus = status;
        this.identityLoading = false;
      },
      error: (error) => {
        console.error('Erreur statut identité:', error);
        this.identityError = 'Impossible de récupérer le statut d’identité.';
        this.identityLoading = false;
      }
    });
  }

  loadDriverProfile(): void {
    if (!this.user) {
      return;
    }
    this.driverProfileLoading = true;
    this.driverProfileMessage = '';
    this.driverProfileService.getProfile().subscribe({
      next: (profile) => {
        this.driverProfile = profile ?? createEmptyDriverProfile();
        this.driverProfileLoading = false;
      },
      error: (error) => {
        console.error('Erreur profil conducteur:', error);
        this.driverProfile = createEmptyDriverProfile();
        this.driverProfileLoading = false;
        this.driverProfileMessage = 'Profil conducteur indisponible pour le moment.';
      }
    });
  }

  loadBookingHistory(): void {
    if (!this.user) {
      return;
    }
    this.bookingHistoryLoading = true;
    this.bookingHistoryError = '';
    this.bookingsService.getBookingsByUser(this.user.id).subscribe({
      next: (bookings) => {
        this.bookingHistory = bookings ?? [];
        this.bookingHistoryLoading = false;
      },
      error: (error) => {
        console.error('Erreur historique réservations:', error);
        this.bookingHistory = [];
        this.bookingHistoryLoading = false;
        this.bookingHistoryError = 'Historique indisponible pour le moment.';
      }
    });
  }

  saveDriverProfile(): void {
    if (this.driverProfileSaving) {
      return;
    }
    this.driverProfileSaving = true;
    this.driverProfileMessage = '';
    this.driverProfileService.saveProfile(this.driverProfile).subscribe({
      next: (profile) => {
        this.driverProfile = profile;
        this.driverProfileSaving = false;
        this.driverProfileMessage = 'Profil conducteur enregistré.';
        this.notificationService.success('Profil conducteur mis à jour.');
      },
      error: (error) => {
        console.error('Erreur sauvegarde profil conducteur:', error);
        this.driverProfileSaving = false;
        this.driverProfileMessage = 'Impossible de sauvegarder le profil pour le moment.';
        this.notificationService.error('Sauvegarde du profil conducteur impossible.');
      }
    });
  }

  saveMobilityPreferences(): void {
    localStorage.setItem('mobilityPreferences', JSON.stringify(this.mobilityPreferences));
    this.notificationService.success('Préférences de mobilité mises à jour.');
  }

  saveSecurityPreferences(): void {
    localStorage.setItem('securityPreferences', JSON.stringify(this.securityPreferences));
    this.notificationService.success('Paramètres d’alertes mis à jour.');
  }

  private loadLocalPreferences(): void {
    const storedMobility = localStorage.getItem('mobilityPreferences');
    if (storedMobility) {
      try {
        this.mobilityPreferences = { ...this.mobilityPreferences, ...JSON.parse(storedMobility) };
      } catch {
        // ignore
      }
    }
    const storedSecurity = localStorage.getItem('securityPreferences');
    if (storedSecurity) {
      try {
        this.securityPreferences = { ...this.securityPreferences, ...JSON.parse(storedSecurity) };
      } catch {
        // ignore
      }
    }
  }

  private loadAvatarFromServer(): void {
    if (this.user?.avatarUrl) {
      this.avatarPreview = this.user.avatarUrl;
    }
    this.profileService.getAvatar().subscribe({
      next: (response) => {
        if (response?.url) {
          const absolute = this.buildAvatarUrl(response.url);
          this.avatarPreview = absolute;
          this.updateUserAvatar(response.url);
        }
      },
      error: () => {
        // silencieux si pas d'avatar
      }
    });
  }

  private buildAvatarUrl(relativeUrl?: string | null): string | null {
    if (!relativeUrl) {
      return null;
    }
    if (relativeUrl.startsWith('http')) {
      return relativeUrl;
    }
    return `${environment.apiUrl}${relativeUrl}`;
  }

  private updateUserAvatar(relativeUrl: string | null): void {
    if (!this.user) {
      return;
    }
    const absolute = this.buildAvatarUrl(relativeUrl);
    this.user.avatarUrl = absolute ?? undefined;
    this.authService.updateCurrentUser(this.user);
  }

  async startIdentityVerification(): Promise<void> {
    if (this.identityActionLoading) {
      return;
    }
    this.identityActionLoading = true;
    this.identityError = '';
    try {
      const session = await firstValueFrom(
        this.identityService.createSession(`${window.location.origin}/profile/identity`)
      );
      const stripe = await this.stripePromise;
      if (!stripe) {
        throw new Error('Stripe non initialisé côté client');
      }
      const { error } = await stripe.verifyIdentity(session.clientSecret);
      if (error) {
        console.error('Erreur Stripe Identity', error);
        this.identityError = error.message ?? 'La vérification a été interrompue.';
        this.notificationService.error(this.identityError);
      } else {
        this.notificationService.success(
          'Merci ! Documents envoyés, nous vous notifierons dès validation.'
        );
      }
    } catch (error: any) {
      console.error('Impossible de démarrer la vérification', error);
      const message = error?.message || 'Impossible de lancer la vérification.';
      this.identityError = message;
      this.notificationService.error(message);
    } finally {
      this.identityActionLoading = false;
      this.fetchIdentityStatus();
    }
  }

  onAvatarSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) {
      return;
    }
    const file = input.files[0];
    this.avatarMessage = 'Téléchargement en cours...';
    this.profileService.uploadAvatar(file).subscribe({
      next: (response) => {
        this.avatarPreview = this.buildAvatarUrl(response?.url ?? null);
        this.updateUserAvatar(response?.url ?? null);
        this.avatarMessage = 'Avatar mis à jour.';
      },
      error: (error) => {
        console.error('Erreur upload avatar', error);
        this.avatarMessage = 'Impossible de télécharger cet avatar.';
        this.notificationService.error('Téléversement avatar impossible.');
      }
    });
  }

  clearAvatar(): void {
    this.profileService.deleteAvatar().subscribe({
      next: () => {
        this.avatarPreview = null;
        this.avatarMessage = 'Avatar réinitialisé.';
        this.updateUserAvatar(null);
      },
      error: (error) => {
        console.error('Erreur suppression avatar', error);
        this.notificationService.error('Impossible de réinitialiser l’avatar.');
      }
    });
  }

  refreshIdentityStatus(): void {
    this.fetchIdentityStatus();
  }

  getIdentityLabel(): string {
    const status = this.identityStatus?.status?.toUpperCase() ?? 'NONE';
    switch (status) {
      case 'VERIFIED':
        return 'Identité vérifiée';
      case 'PROCESSING':
        return 'Analyse en cours';
      case 'REQUIRES_INPUT':
        return 'Documents incomplets';
      case 'PENDING':
      case 'NONE':
        return 'Vérification non réalisée';
      case 'REJECTED':
        return 'Documents rejetés';
      default:
        return status;
    }
  }

  getIdentityBadgeClass(): string {
    const status = this.identityStatus?.status?.toUpperCase() ?? 'NONE';
    if (status === 'VERIFIED') return 'badge-success';
    if (status === 'REQUIRES_INPUT' || status === 'REJECTED') return 'badge-danger';
    if (status === 'PROCESSING') return 'badge-info';
    return 'badge-muted';
  }

  loadUserStats(): void {
    // Pour l'instant, données mockées
    // Plus tard, appeler un service pour les vraies statistiques
    this.userStats = {
      totalBookings: 12,
      activeBookings: 3,
      cancelledBookings: 2,
      completedBookings: 7
    };
  }


  // NOUVELLE MÉTHODE : Activer le mode édition
  editProfile(): void {
    this.isEditing = true;
    // Pré-remplir le formulaire avec les données actuelles
    this.editForm = {
      username: this.user?.username || '',
      email: this.user?.email || ''
    };
  }

  // NOUVELLE MÉTHODE : Sauvegarder les modifications
  saveProfile(): void {
    if (!this.user) return;

    this.isLoading = true;
    
    // Simuler la sauvegarde (à remplacer par un vrai appel API)
    setTimeout(() => {
      // Mettre à jour l'utilisateur localement
      if (this.user) {
        this.user.username = this.editForm.username;
        this.user.email = this.editForm.email;
        
        // Mettre à jour dans le service d'authentification
        this.authService.updateCurrentUser(this.user);
      }
      
      this.isEditing = false;
      this.isLoading = false;
      
      // Afficher un message de succès
      console.log('✅ Profil mis à jour avec succès');
    }, 1000);
  }

  // NOUVELLE MÉTHODE : Annuler l'édition
  cancelEdit(): void {
    this.isEditing = false;
    this.editForm = { username: '', email: '' };
  }

  getInitials(): string {
    if (!this.user?.username) return '?';
    return this.user.username.charAt(0).toUpperCase();
  }

  getRoleText(): string {
    switch (this.user?.role) {
      case 'ROLE_ADMIN': return 'Administrateur';
      case 'ROLE_USER': return 'Utilisateur';
      default: return 'Utilisateur';
    }
  }

  getRoleClass(): string {
    switch (this.user?.role) {
      case 'ROLE_ADMIN': return 'role-admin';
      case 'ROLE_USER': return 'role-user';
      default: return 'role-user';
    }
  }

  getMemberSince(): string {
    // Pour l'instant, date fictive
    return 'Octobre 2024';
  }

   

  goToBookings(): void {
    this.router.navigate(['/bookings']);
  }

  goToDashboard(): void {
    this.router.navigate(['/dashboard']);
  }

  goToAdminHome(): void {
    this.router.navigate(['/admin']);
  }

  goToAdminFinance(): void {
    this.router.navigate(['/admin/finance']);
  }

  goToAdminUsers(): void {
    this.router.navigate(['/admin/users']);
  }

  goToAdminBookings(): void {
    this.router.navigate(['/admin/bookings']);
  }

  getBookingStatusBadge(status?: string | null): string {
    const value = (status ?? '').toUpperCase();
    switch (value) {
      case 'COMPLETED':
        return 'status-badge success';
      case 'CONFIRMED':
        return 'status-badge info';
      case 'CANCELLED':
        return 'status-badge danger';
      default:
        return 'status-badge muted';
    }
  }

  getTimelineIcon(status?: string | null): string {
    const value = (status ?? '').toUpperCase();
    if (value === 'COMPLETED') return '✅';
    if (value === 'CONFIRMED') return '🟢';
    if (value === 'CANCELLED') return '⚠️';
    return '⏳';
  }

  formatBookingDate(booking: Booking): string {
    return booking.reservationDate
      ? new Date(booking.reservationDate).toLocaleString('fr-FR', {
        day: '2-digit',
        month: 'short',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
      : '—';
  }

  navigateToBooking(booking: Booking): void {
    if (!booking?.reservationId) {
      return;
    }
    this.router.navigate(['/bookings'], {
      queryParams: { reservationId: booking.reservationId }
    });
  }

  async retryPayment(booking: Booking): Promise<void> {
    if (!booking?.reservationId) {
      return;
    }
    try {
      await this.paymentService.startCheckout(booking.reservationId);
    } catch (error: any) {
      console.error('Impossible de relancer le paiement', error);
      this.notificationService.error(error?.message ?? 'Relance paiement impossible.');
    }
  }

  downloadReceipt(booking: Booking): void {
    if (!booking?.reservationId) {
      return;
    }
    this.bookingsService.downloadReceipt(booking.reservationId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const anchor = document.createElement('a');
        anchor.href = url;
        anchor.download = `recu-reservation-${booking.reservationId}.pdf`;
        anchor.click();
        window.URL.revokeObjectURL(url);
      },
      error: (error) => {
        console.error('Erreur téléchargement reçu', error);
        this.notificationService.error('Impossible de télécharger le reçu.');
      }
    });
  }

  canRetryPayment(booking: Booking): boolean {
    const status = (booking?.paymentStatus ?? '').toUpperCase();
    return status === 'PENDING' || status === 'REQUIRES_ACTION' || status === 'FAILED' || status === 'EXPIRED';
  }

  canDownloadReceipt(booking: Booking): boolean {
    const status = (booking?.paymentStatus ?? '').toUpperCase();
    return status === 'PAID' || status === 'COMPLETED';
  }

}
