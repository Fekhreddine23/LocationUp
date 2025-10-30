import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { OffersService } from '../../core/services/offers.service';
import { OfferCardComponent } from '../../components/offer-card/offer-card';
import { Offer } from '../../core/models/offer.model';
import { Breadcrumbs } from "../../components/breadcrumbs/breadcrumbs";


@Component({
  selector: 'app-offers',
  standalone: true,
  imports: [CommonModule, OfferCardComponent, Breadcrumbs], // ← Importer OfferCardComponent
  templateUrl: './offers.component.html',
  styleUrl: './offers.component.scss'
})
export class OffersComponent implements OnInit {
  offers: Offer[] = [];
  isLoading = false;
  errorMessage = '';

  constructor(
    private offersService: OffersService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadOffers();
  }

  loadOffers(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.offersService.getAllOffers().subscribe({
      next: (offers) => {
        this.offers = offers;
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Erreur lors du chargement des offres';
        this.isLoading = false;
        console.error('Error loading offers:', error);
      }
    });
  }

  // Ces méthodes ne sont plus nécessaires car la navigation est gérée dans offer-card
  // Mais on les garde pour éviter les erreurs, elles peuvent être vides
  onReserveOffer(offerId: number): void {
    // La navigation est maintenant gérée directement dans offer-card
    console.log('Reserve offer:', offerId);
  }

  onViewOfferDetails(offerId: number): void {
    // La navigation est maintenant gérée directement dans offer-card
    console.log('View details:', offerId);
  }

  breadcrumbItems = [ //breadcrumbs statiques navigation
  { label: 'Tableau de Bord', url: '/dashboard' },
  { label: 'Offres Disponibles', url: '/offers', active: true }
];
}