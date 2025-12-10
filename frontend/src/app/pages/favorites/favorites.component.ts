import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';

import { Offer } from '../../core/models/offer.model';
import { OffersService } from '../../core/services/offers.service';
import { OfferCardComponent } from '../../components/offer-card/offer-card';
import { Breadcrumbs } from '../../components/breadcrumbs/breadcrumbs';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-favorites',
  standalone: true,
  imports: [CommonModule, OfferCardComponent, Breadcrumbs],
  templateUrl: './favorites.component.html',
  styleUrl: './favorites.component.scss'
})
export class FavoritesComponent implements OnInit, OnDestroy {

  favorites: Offer[] = [];
  isLoading = true;
  errorMessage = '';
  private subscriptions = new Subscription();

  readonly breadcrumbItems = [
    { label: 'Tableau de bord', url: '/dashboard' },
    { label: 'Mes favoris', url: '/favorites', active: true }
  ];

  constructor(
    private offersService: OffersService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (!this.authService.isLoggedIn()) {
      this.errorMessage = 'Vous devez être connecté pour accéder à vos favoris.';
      this.isLoading = false;
      return;
    }

    this.subscriptions.add(
      this.offersService.getFavoriteOffersStream().subscribe(favs => {
        this.favorites = favs;
        this.isLoading = false;
      })
    );

    // assure une synchro à l’ouverture de la page
    this.offersService.refreshServerFavorites();
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  toggleFavorite(offer: Offer): void {
    this.offersService.toggleFavorite(offer).subscribe({
      error: () => this.errorMessage = 'Impossible de mettre à jour ce favori.'
    });
  }

  goToOffers(): void {
    this.router.navigate(['/offers']);
  }

  trackByOffer(_: number, offer: Offer): number {
    return offer.offerId;
  }
}
