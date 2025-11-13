import { Component, OnInit } from '@angular/core';
import { Breadcrumbs } from '../../components/breadcrumbs/breadcrumbs';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { User } from '../../core/models/auth.models';
import { UserStatsService } from '../../core/services/user-stats.service';
import { TwoFactorSetup } from '../../components/twoFactor/two-factor-setup';

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

  constructor(
    private authService: AuthService,
    private userStatsService: UserStatsService,
    private router: Router
  ) {}

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
      },
      error: (error) => {
        console.error('Erreur chargement statistiques:', error);
        this.errorMessage = 'Erreur lors du chargement des statistiques';
        this.isLoading = false;
      }
    });

    
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
