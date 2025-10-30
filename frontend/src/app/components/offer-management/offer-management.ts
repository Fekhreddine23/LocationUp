import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { Offer, OfferStatus } from '../../core/models/offer.model';
import { OfferResponse } from '../../core/models/OfferReponse.model';
import { AdminService } from '../../core/services/admin.service';
import { Breadcrumbs } from '../breadcrumbs/breadcrumbs';

type AdminOffer = Offer & {
  status?: OfferStatus;
  pickupLocation?: string;
  returnLocation?: string;
  mobilityService?: string;
  adminName?: string;
};

type AdminOfferForm = {
  pickupLocation: string;
  returnLocation: string;
  mobilityService: string;
  pickupDatetime: string;
  description?: string;
  price: number;
  status?: OfferStatus;
};

@Component({
  selector: 'app-offer-management',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, Breadcrumbs],
  templateUrl: './offer-management.html',
  styleUrl: './offer-management.scss'
})
export class OfferManagement implements OnInit {
  private static readonly DEFAULT_STATUS: OfferStatus = 'ACTIVE';
  private static readonly STATUS_VALUES: readonly OfferStatus[] = ['ACTIVE', 'INACTIVE', 'EXPIRED'];

  offers: AdminOffer[] = [];
  filteredOffers: AdminOffer[] = [];
  selectedOffer: AdminOffer | null = null;
  
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

  breadcrumbItems = [
    { label: 'Administration', url: '/admin' },
    { label: 'Gestion des Offres', url: '/admin/offers', active: true }
  ];

  constructor(
    private adminService: AdminService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadOffers();
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
      },
      error: (error) => {
        console.error('Erreur chargement offres:', error);
        this.errorMessage = 'Erreur lors du chargement des offres';
        this.isLoading = false;
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
      filtered = filtered.filter(offer => 
        (offer.pickupLocation ?? '').toLowerCase().includes(this.locationFilter.toLowerCase()) ||
        (offer.returnLocation ?? '').toLowerCase().includes(this.locationFilter.toLowerCase())
      );
    }

    this.filteredOffers = filtered;
  }

  // Actions sur les offres
  createOffer(): void {
    this.isCreateModalOpen = true;
  }

  submitNewOffer(): void {
    this.adminService.createOffer(this.newOffer).subscribe({
      next: (createdOffer) => {
        this.offers.unshift(createdOffer);
        this.filteredOffers = [...this.offers];
        this.successMessage = 'Offre créée avec succès';
        this.isCreateModalOpen = false;
        this.resetNewOfferForm();
        this.clearMessagesAfterDelay();
      },
      error: (error) => {
        console.error('Erreur création:', error);
        this.errorMessage = 'Erreur lors de la création de l\'offre';
        this.clearMessagesAfterDelay();
      }
    });
  }

  editOffer(offer: AdminOffer): void {
    this.selectedOffer = { ...offer };
    this.isEditModalOpen = true;
  }

  updateOffer(): void {
    if (!this.selectedOffer) return;

    this.adminService.updateOffer(this.selectedOffer.offerId, this.selectedOffer).subscribe({
      next: (updatedOffer) => {
        const index = this.offers.findIndex(o => o.offerId === updatedOffer.offerId);
        if (index !== -1) {
          this.offers[index] = updatedOffer;
          this.filteredOffers = [...this.offers];
        }
        this.successMessage = 'Offre mise à jour avec succès';
        this.isEditModalOpen = false;
        this.clearMessagesAfterDelay();
      },
      error: (error) => {
        console.error('Erreur mise à jour:', error);
        this.errorMessage = 'Erreur lors de la mise à jour';
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
      },
      error: (error) => {
        console.error('Erreur suppression:', error);
        this.errorMessage = 'Erreur lors de la suppression';
        this.clearMessagesAfterDelay();
      }
    });
  }

  toggleOfferStatus(offer: AdminOffer): void {
    const currentStatus = OfferManagement.normalizeStatus(offer.status);
    const newStatus: OfferStatus = currentStatus === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    
    this.adminService.changeOfferStatus(offer.offerId, newStatus).subscribe({
      next: (updatedOffer) => {
        offer.status = OfferManagement.normalizeStatus(updatedOffer.status ?? newStatus);
        this.successMessage = `Offre ${offer.status === 'ACTIVE' ? 'activée' : 'désactivée'} avec succès`;
        this.clearMessagesAfterDelay();
      },
      error: (error) => {
        console.error('Erreur changement statut:', error);
        this.errorMessage = 'Erreur lors du changement de statut';
        this.clearMessagesAfterDelay();
      }
    });
  }

  // Utilitaires
  getStatusText(status?: string): string {
    const statusMap: { [key: string]: string } = {
      'ACTIVE': 'Active',
      'INACTIVE': 'Inactive',
      'EXPIRED': 'Expirée'
    };
    const key = OfferManagement.normalizeStatus(status);
    return statusMap[key] || key;
  }

  getStatusClass(status?: string): string {
    return OfferManagement.normalizeStatus(status) === 'ACTIVE' ? 'status-active' : 'status-inactive';
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
  }

  isValidOffer(): boolean {
    return !!(
      this.newOffer.pickupLocation &&
      this.newOffer.returnLocation &&
      this.newOffer.mobilityService &&
      this.newOffer.pickupDatetime &&
      (this.newOffer.price ?? 0) > 0
    );
  }

  private createEmptyOfferForm(): AdminOfferForm {
    return {
      pickupLocation: '',
      returnLocation: '',
      mobilityService: '',
      pickupDatetime: '',
      description: '',
      price: 0,
      status: 'ACTIVE'
    };
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

  // Navigation
  goBackToDashboard(): void {
    this.router.navigate(['/admin']);
  }

}
