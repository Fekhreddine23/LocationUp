import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { CreateOfferRequest, OfferStatus } from '../../core/models/offer.model';
import { OfferResponse } from '../../core/models/OfferReponse.model';
import { AdminService } from '../../core/services/admin.service';
import { Breadcrumbs } from '../breadcrumbs/breadcrumbs';
import { AdminOffer, AdminOfferForm } from '../../core/models/AdminOffer.model';
import { MobilityService as MobilityServicesService } from '../../core/services/mobility-services.service';
import { MobilityService as MobilityServiceModel } from '../../core/models/MobilityService.model';

@Component({
  selector: 'app-offer-management',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, Breadcrumbs],
  templateUrl: './offer-management.html',
  styleUrl: './offer-management.scss'
})
export class OfferManagement implements OnInit {
  private static readonly DEFAULT_STATUS: OfferStatus = 'PENDING';
  private static readonly STATUS_VALUES: readonly OfferStatus[] = ['PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED'] as const;

  offers: AdminOffer[] = [];
  filteredOffers: AdminOffer[] = [];
  selectedOffer: AdminOffer | null = null;
  hasLoadedInitialData = false;

  // Données pour les formulaires
  mobilityServices: MobilityServiceModel[] = [];
  mobilityCategories: string[] = ['AUTRE'];
  isLoadingServices = false;

  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;

  // Filtres
  searchQuery = '';
  statusFilter: 'ALL' | OfferStatus = 'ALL';
  locationFilter = '';

  // États
  isLoading = false;
  isEditModalOpen = false;
  isCreateModalOpen = false;
  isDeleteModalOpen = false;

  // Messages
  successMessage = '';
  errorMessage = '';

  // Formulaire nouvelle offre
  newOffer: AdminOfferForm = this.createEmptyOfferForm();

  // Statistiques
  stats: any = {};
  readonly skeletonFilters = Array.from({ length: 3 });
  readonly skeletonCards = Array.from({ length: 4 });
  readonly skeletonCardLines = Array.from({ length: 5 });
  readonly skeletonActionButtons = Array.from({ length: 3 });

  breadcrumbItems = [
    { label: 'Administration', url: '/admin' },
    { label: 'Gestion des Offres', url: '/admin/offers', active: true }
  ];

  constructor(
    private adminService: AdminService,
    private router: Router,
    private mobilityServiceApi: MobilityServicesService
  ) { }

  ngOnInit(): void {
    this.loadOffers();
    this.loadStats();
    this.loadMobilityServices();
  }

  loadOffers(): void {
    this.isLoading = true;
    this.adminService.getAllOffers(this.currentPage, this.pageSize).subscribe({
      next: (response: OfferResponse) => {
        this.offers = response.content;
        this.filteredOffers = [...this.offers];
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.isLoading = false;
        this.hasLoadedInitialData = true;
      },
      error: (error) => {
        console.error('Erreur chargement offres:', error);
        this.errorMessage = 'Erreur lors du chargement des offres';
        this.isLoading = false;
        this.hasLoadedInitialData = true;
      }
    });
  }

  searchOffers(): void {
    if (this.searchQuery.trim()) {
      this.isLoading = true;
      this.adminService.searchOffers(this.searchQuery, this.currentPage, this.pageSize).subscribe({
        next: (response: OfferResponse) => {
          this.filteredOffers = response.content;
          this.totalElements = response.totalElements;
          this.totalPages = response.totalPages;
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Erreur recherche:', error);
          this.errorMessage = 'Erreur lors de la recherche';
          this.isLoading = false;
        }
      });
    } else {
      this.filteredOffers = [...this.offers];
    }
  }

  applyFilters(): void {
    let filtered = [...this.offers];

    // Filtre par statut
    if (this.statusFilter !== 'ALL') {
      filtered = filtered.filter(
        offer => OfferManagement.normalizeStatus(offer.status) === this.statusFilter
      );
    }

    // Filtre par lieu
    if (this.locationFilter) {
      const needle = this.locationFilter.toLowerCase();
      filtered = filtered.filter(offer => {
        const pickup = (offer.pickupLocationName ?? offer.pickupLocation ?? '').toLowerCase();
        const dropoff = (offer.returnLocationName ?? offer.returnLocation ?? '').toLowerCase();
        return pickup.includes(needle) || dropoff.includes(needle);
      });
    }

    this.filteredOffers = filtered;
  }

  // Chargement des services de mobilité
  loadMobilityServices(): void {
    this.isLoadingServices = true;
    this.mobilityServiceApi.getAllServices().subscribe({
      next: (services) => {
        this.mobilityServices = services;
        const categories = services
          .map(service => (service.category ?? service.name ?? '').trim())
          .filter((category): category is string => category.length > 0);

        this.mobilityCategories = Array.from(new Set(categories)).sort((a, b) => a.localeCompare(b));

        if (!this.mobilityCategories.includes('AUTRE')) {
          this.mobilityCategories.push('AUTRE');
        }

        if (!this.newOffer.mobilityService && this.mobilityCategories.length) {
          this.newOffer.mobilityService = this.mobilityCategories[0];
        }

        if (this.selectedOffer?.mobilityService && this.mobilityCategories.length) {
          const category = this.selectedOffer.mobilityService;
          if (!this.mobilityCategories.includes(category)) {
            this.mobilityCategories = [category, ...this.mobilityCategories];
          } else {
            this.mobilityCategories = [
              category,
              ...this.mobilityCategories.filter(item => item !== category)
            ];
          }
        }

        this.isLoadingServices = false;
      },
      error: (error) => {
        console.error('❌ Erreur chargement services:', error);
        if (!this.mobilityCategories.length) {
          this.mobilityCategories = ['AUTRE'];
        }
        if (!this.newOffer.mobilityService) {
          this.newOffer.mobilityService = this.mobilityCategories[0];
        }
        this.isLoadingServices = false;
      }
    });
  }

  // Actions sur les offres
  createOffer(): void {
    this.isCreateModalOpen = true;
  }

  submitNewOffer(): void {
    let payload: CreateOfferRequest;
    try {
      payload = this.buildCreateOfferPayload(this.newOffer);
      console.log('🎯 Données envoyées au backend:', payload); // DEBUG
    } catch (error) {
      console.error('Validation formulaire offre:', error);
      this.errorMessage = error instanceof Error ? error.message : 'Données du formulaire invalides';
      this.clearMessagesAfterDelay();
      return;
    }

    this.adminService.createOffer(payload).subscribe({
      next: (createdOffer) => {
        this.offers.unshift(createdOffer);
        this.filteredOffers = [...this.offers];
        this.successMessage = 'Offre créée avec succès';
        this.isCreateModalOpen = false;
        this.resetNewOfferForm();
        this.clearMessagesAfterDelay();
        this.loadStats();
      },
      error: (error) => {
        console.error('❌ Erreur création:', error);
        this.errorMessage = error.message || 'Erreur lors de la création de l\'offre';
        this.clearMessagesAfterDelay();
      }
    });
  }

  editOffer(offer: AdminOffer): void {
    this.selectedOffer = { ...offer };

    this.selectedOffer.pickupLocation = this.selectedOffer.pickupLocation ?? this.selectedOffer.pickupLocationName ?? '';
    this.selectedOffer.pickupLocationCity = this.selectedOffer.pickupLocationCity ?? this.selectedOffer.pickupLocationName ?? this.selectedOffer.pickupLocation ?? '';
    this.selectedOffer.returnLocation = this.selectedOffer.returnLocation ?? this.selectedOffer.returnLocationName ?? '';
    this.selectedOffer.returnLocationCity = this.selectedOffer.returnLocationCity ?? this.selectedOffer.returnLocationName ?? this.selectedOffer.returnLocation ?? '';

    if (this.mobilityCategories.length && this.selectedOffer.mobilityService) {
      if (!this.mobilityCategories.includes(this.selectedOffer.mobilityService)) {
        this.mobilityCategories = [
          this.selectedOffer.mobilityService,
          ...this.mobilityCategories
        ];
      }
    } else if (this.mobilityCategories.length) {
      this.selectedOffer.mobilityService = this.mobilityCategories[0];
    }

    this.isEditModalOpen = true;
  }

  updateOffer(): void {
    if (!this.selectedOffer) return;

    console.log('🔄 Début mise à jour offre:', this.selectedOffer);

    // Validation
    const pickupLocation = (this.selectedOffer.pickupLocation ?? '').trim();
    const returnLocation = (this.selectedOffer.returnLocation ?? '').trim();
    const mobilityService = (this.selectedOffer.mobilityService ?? '').trim();
    const price = Number(this.selectedOffer.price);

    if (!pickupLocation || !returnLocation || !mobilityService || !Number.isFinite(price) || price <= 0) {
      this.errorMessage = 'Veuillez renseigner les champs obligatoires';
      this.clearMessagesAfterDelay();
      return;
    }

    // Trouver le service
    const selectedService = this.mobilityServices.find(service =>
      service.category?.toLowerCase() === mobilityService.toLowerCase() ||
      service.name?.toLowerCase() === mobilityService.toLowerCase()
    );

    if (!selectedService) {
      this.errorMessage = 'Service de mobilité non trouvé';
      this.clearMessagesAfterDelay();
      return;
    }

    // 🎯 PAYLOAD CORRECT POUR LE BACKEND
  const payload: any = {
    // Champs de base
    description: this.selectedOffer.description || '',
    price: price,
    pickupDatetime: this.selectedOffer.pickupDatetime,
    status: this.selectedOffer.status || 'PENDING',
    active: this.selectedOffer.active !== false,
    
    // 🎯 CHAMPS CRITIQUES POUR LA MISE À JOUR DES VILLES
    pickupLocationName: pickupLocation,    // ← Doit être envoyé
    returnLocationName: returnLocation,    // ← Doit être envoyé
    mobilityServiceId: selectedService?.serviceId, // ← Doit être envoyé
    adminId: this.toPositiveNumber(this.selectedOffer.adminId),
  };

  console.log('📤 Payload de mise à jour:', payload);

    console.log('📤 Payload envoyé:', payload);

    this.adminService.updateOffer(this.selectedOffer.offerId, payload).subscribe({
      next: (updatedOffer) => {
        console.log('✅ Réponse backend:', updatedOffer);
        this.updateOfferLocally(updatedOffer);
        this.successMessage = 'Offre mise à jour avec succès';
        this.isEditModalOpen = false;
        this.selectedOffer = null; // ← IMPORTANT: Réinitialiser
        this.clearMessagesAfterDelay();
        this.loadStats();
      },
      error: (error) => {
        console.error('❌ Erreur détaillée:', error);
        this.errorMessage = `Erreur lors de la mise à jour: ${error.message || 'Erreur serveur'}`;
        this.clearMessagesAfterDelay();
      }
    });
  }

  confirmDelete(offer: AdminOffer): void {
    this.selectedOffer = offer;
    this.isDeleteModalOpen = true;
  }

  deleteOffer(): void {
    if (!this.selectedOffer) return;

    this.adminService.deleteOffer(this.selectedOffer.offerId).subscribe({
      next: () => {
        this.offers = this.offers.filter(o => o.offerId !== this.selectedOffer!.offerId);
        this.filteredOffers = this.filteredOffers.filter(o => o.offerId !== this.selectedOffer!.offerId);
        this.successMessage = 'Offre supprimée avec succès';
        this.isDeleteModalOpen = false;
        this.clearMessagesAfterDelay();
        this.loadStats();
      },
      error: (error) => {
        console.error('Erreur suppression:', error);
        this.errorMessage = 'Erreur lors de la suppression';
        this.clearMessagesAfterDelay();
      }
    });
  }

  // Utilitaires
  getStatusText(status?: string): string {
    const statusMap: Record<OfferStatus, string> = {
      PENDING: 'En attente',
      CONFIRMED: 'Confirmée',
      CANCELLED: 'Annulée',
      COMPLETED: 'Terminée'
    };
    const key = OfferManagement.normalizeStatus(status);
    return statusMap[key] || key;
  }

  getStatusClass(status?: string): string {
    const classMap: Record<OfferStatus, string> = {
      PENDING: 'status-pending',
      CONFIRMED: 'status-confirmed',
      CANCELLED: 'status-cancelled',
      COMPLETED: 'status-completed'
    };
    return classMap[OfferManagement.normalizeStatus(status)];
  }

  isOfferConfirmed(offer: AdminOffer): boolean {
    return OfferManagement.normalizeStatus(offer.status) === 'CONFIRMED';
  }

  canToggleStatus(offer: AdminOffer): boolean {
    const status = OfferManagement.normalizeStatus(offer.status);
    return status !== 'COMPLETED';
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: 'EUR'
    }).format(amount);
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('fr-FR', {
      day: 'numeric',
      month: 'long',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  resetNewOfferForm(): void {
    this.newOffer = this.createEmptyOfferForm();
    if (this.mobilityCategories.length) {
      this.newOffer.mobilityService = this.mobilityCategories[0];
    }
  }

  isValidOffer(): boolean {
    const adminId = this.toPositiveNumber(this.newOffer.adminId);
    const pickupLocation = (this.newOffer.pickupLocation ?? '').trim();
    const pickupCity = (this.newOffer.pickupLocationCity ?? '').trim();
    const returnLocation = (this.newOffer.returnLocation ?? '').trim();
    const returnCity = (this.newOffer.returnLocationCity ?? '').trim();
    const mobilityService = (this.newOffer.mobilityService ?? '').trim();
    const price = Number(this.newOffer.price);

    return !!(
      pickupLocation &&
      pickupCity &&
      returnLocation &&
      returnCity &&
      mobilityService &&
      this.newOffer.pickupDatetime &&
      Number.isFinite(price) &&
      price > 0 &&
      adminId
    );
  }

  private createEmptyOfferForm(): AdminOfferForm {
    // Date future par défaut (demain à 10h)
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    tomorrow.setHours(10, 0, 0, 0);

    return {
      pickupLocation: '',
      pickupLocationCity: '',
      returnLocation: '',
      returnLocationCity: '',
      mobilityService: this.mobilityCategories[0] ?? '',
      pickupDatetime: tomorrow.toISOString().slice(0, 16),
      description: '',
      price: 0,
      status: 'PENDING',
      adminId: null
    };
  }

  clearMessagesAfterDelay(): void {
    setTimeout(() => {
      this.successMessage = '';
      this.errorMessage = '';
    }, 5000);
  }

  // Pagination
  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadOffers();
    }
  }

  // Statistiques
  loadStats(): void {
    this.adminService.getOfferStats().subscribe({
      next: (stats) => {
        this.stats = stats;
      },
      error: (error) => {
        console.error('Erreur chargement stats:', error);
      }
    });
  }

  // Activation/désactivation
  activateOffer(offer: AdminOffer): void {
    this.adminService.activateOffer(offer.offerId).subscribe({
      next: () => {
        this.applyOfferStatusChange(offer.offerId, 'CONFIRMED');
        this.successMessage = 'Offre confirmée avec succès';
        this.loadStats();
        this.clearMessagesAfterDelay();
      },
      error: (error) => {
        console.error('Erreur activation:', error);
        this.errorMessage = 'Erreur lors de la confirmation';
        this.clearMessagesAfterDelay();
      }
    });
  }

  deactivateOffer(offer: AdminOffer): void {
    this.adminService.deactivateOffer(offer.offerId).subscribe({
      next: () => {
        this.applyOfferStatusChange(offer.offerId, 'CANCELLED');
        this.successMessage = 'Offre annulée avec succès';
        this.loadStats();
        this.clearMessagesAfterDelay();
      },
      error: (error) => {
        console.error('Erreur désactivation:', error);
        this.errorMessage = 'Erreur lors de l\'annulation';
        this.clearMessagesAfterDelay();
      }
    });
  }

  toggleOfferStatus(offer: AdminOffer): void {
    if (!this.canToggleStatus(offer)) {
      return;
    }
    if (this.isOfferConfirmed(offer)) {
      this.deactivateOffer(offer);
    } else {
      this.activateOffer(offer);
    }
  }


  //=======updateOffer() = Sauvegarde les données=============  
  // =========updateOfferLocally() Affiche les données sauvegardées dans la liste =======//
  private updateOfferLocally(updatedOffer: any): void {
    console.log('🔄 Mise à jour locale avec:', updatedOffer);

    const mappedOffer: AdminOffer = {
      ...updatedOffer,
      // S'assurer que les champs d'affichage sont synchronisés
      pickupLocation: updatedOffer.pickupLocationName || updatedOffer.pickupLocation,
      returnLocation: updatedOffer.returnLocationName || updatedOffer.returnLocation,
      mobilityService: updatedOffer.mobilityServiceName || this.getServiceNameById(updatedOffer.mobilityServiceId),
      pickupLocationCity: updatedOffer.pickupLocationName || updatedOffer.pickupLocation,
      returnLocationCity: updatedOffer.returnLocationName || updatedOffer.returnLocation
    };

    // Mise à jour des listes
    this.offers = this.offers.map(offer =>
      offer.offerId === mappedOffer.offerId ? mappedOffer : offer
    );
    this.filteredOffers = this.filteredOffers.map(offer =>
      offer.offerId === mappedOffer.offerId ? mappedOffer : offer
    );

    // Forcer la détection de changement
    this.filteredOffers = [...this.filteredOffers];

    console.log('✅ Liste mise à jour:', this.filteredOffers);
  }

  private getServiceNameById(serviceId: number): string {
    const service = this.mobilityServices.find(s => s.serviceId === serviceId);
    return service?.name || service?.category || 'Service inconnu';
  }

  private applyOfferStatusChange(offerId: number, status: OfferStatus): void {
    const applyStatus = (offer: AdminOffer): AdminOffer =>
      offer.offerId === offerId ? { ...offer, status } : offer;

    this.offers = this.offers.map(applyStatus);
    this.filteredOffers = this.filteredOffers.map(applyStatus);

    if (this.selectedOffer?.offerId === offerId) {
      this.selectedOffer = { ...this.selectedOffer, status };
    }

    if (!this.searchQuery.trim()) {
      this.applyFilters();
    }
  }

  private toPositiveNumber(value: unknown): number | undefined {
    const num = Number(value);
    return Number.isFinite(num) && num > 0 ? num : undefined;
  }

  // Navigation
  goBackToDashboard(): void {
    this.router.navigate(['/admin']);
  }

  private static normalizeStatus(status?: string | null): OfferStatus {
    if (!status) {
      return OfferManagement.DEFAULT_STATUS;
    }
    const upper = status.toUpperCase() as OfferStatus;
    return OfferManagement.STATUS_VALUES.includes(upper)
      ? upper
      : OfferManagement.DEFAULT_STATUS;
  }

  /**
   * CORRECTION CRITIQUE : Cette méthode mappe les données du formulaire
   * vers le format attendu par le backend
   */
  private buildCreateOfferPayload(form: AdminOfferForm): CreateOfferRequest {
    const pickupLocation = (form.pickupLocation ?? '').trim();
    const pickupCity = (form.pickupLocationCity ?? '').trim();
    const returnLocation = (form.returnLocation ?? '').trim();
    const returnCity = (form.returnLocationCity ?? '').trim();
    const mobilityService = (form.mobilityService ?? '').trim();
    const price = Number(form.price);

    // Validation
    if (!pickupLocation || !pickupCity || !returnLocation || !returnCity || !mobilityService) {
      throw new Error('Tous les champs requis doivent être fournis');
    }
    if (!Number.isFinite(price) || price <= 0) {
      throw new Error('Le prix doit être supérieur à 0');
    }

    // CORRECTION : Trouver l'ID du service de mobilité
    const matchedService = this.mobilityServices.find(service =>
      service.category?.toLowerCase() === mobilityService.toLowerCase() ||
      service.name?.toLowerCase() === mobilityService.toLowerCase()
    );

    if (!matchedService) {
      throw new Error(`Service de mobilité "${mobilityService}" non trouvé`);
    }

    // CORRECTION : Construire le payload avec les bons noms de champs
    const payload: CreateOfferRequest = {
      pickupLocationName: pickupLocation,    // ← CORRIGÉ : pickupLocationName au lieu de pickupLocation
      returnLocationName: returnLocation,    // ← CORRIGÉ : returnLocationName au lieu de returnLocation
      mobilityServiceId: matchedService.serviceId, // ← CORRIGÉ : mobilityServiceId (number) au lieu de mobilityService (string)
      pickupDatetime: form.pickupDatetime,
      price: price,
      description: form.description?.trim() ?? '',
      status: form.status ?? OfferManagement.DEFAULT_STATUS,
      active: true
    };

    const adminId = this.toPositiveNumber(form.adminId);
    if (adminId) {
      payload.adminId = adminId;
    }

    return payload;
  }
}
