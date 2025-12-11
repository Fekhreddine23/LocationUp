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
  private stripePromise: Promise<Stripe | null>;

  constructor(
    private authService: AuthService,
    private userStatsService: UserStatsService,
    private router: Router,
    private identityService: IdentityService,
    private notificationService: NotificationService
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
      },
      error: (error) => {
        console.error('Erreur chargement statistiques:', error);
        this.errorMessage = 'Erreur lors du chargement des statistiques';
        this.isLoading = false;
        this.fetchIdentityStatus();
      }
    });

    
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

}
