import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { HeaderComponent } from '../../src/app/components/header/header.component';
import { OffersService } from '../../src/app/core/services/offers.service'; // ← SERVICE CORRIGÉ
import { Offer } from './core/models/offer.model';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, CommonModule, HeaderComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App implements OnInit {
  protected readonly title = signal('location-up-frontend');
  offers: Offer[] = []; // ← TYPAGE CORRIGÉ
  error: string = '';
  loading: boolean = true;

  // ⚠️ CORRECTION : Utiliser OffersService au lieu de HttpClient direct
  constructor(private offersService: OffersService) {}

  ngOnInit() {
    this.loadOffers();
  }

  loadOffers() {
    this.loading = true;
    this.error = '';
    
    // ⚠️ CORRECTION : Utiliser le service au lieu d'appel HTTP direct
    this.offersService.getAllOffers().subscribe({
      next: (data) => {
        this.offers = data || [];
        this.loading = false;
        console.log('✅ Offres chargées via service:', this.offers.length);
      },
      error: (err) => {
        this.loading = false;
        
        // Messages d'erreur améliorés
        if (err.status === 0) {
          this.error = '🚨 Backend non disponible - Vérifiez que Spring Boot est démarré';
        } else if (err.status === 403) {
          this.error = '🔒 Accès interdit - Problème de sécurité Spring';
        } else if (err.status === 404) {
          this.error = '📭 Endpoint /offers non trouvé - Utilisation des données mockées';
          // ⚠️ En cas d'erreur 404, utiliser les mocks automatiquement
          this.useMockOffers();
        } else {
          this.error = `Erreur ${err.status}: ${err.message}`;
          this.useMockOffers();
        }
        console.error('❌ Erreur chargement offres:', err);
      }
    });
  }

  // Méthode de fallback avec données mockées
  private useMockOffers(): void {
    console.log('🔄 Utilisation des offres mockées...');
    this.offers = [
      {
        offerId: 1,
        pickupLocationId: 1,
        returnLocationId: 1,
        mobilityServiceId: 1,
        adminId: 1,
        pickupDatetime: new Date(Date.now() + 86400000).toISOString(),
        description: 'Vélo électrique premium - Donnée mockée',
        price: 18.50,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        version: 0
      },
      {
        offerId: 2,
        pickupLocationId: 2,
        returnLocationId: 2,
        mobilityServiceId: 1,
        adminId: 1,
        pickupDatetime: new Date(Date.now() + 172800000).toISOString(),
        description: 'Scooter urbain - Donnée mockée',
        price: 35.00,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        version: 0
      }
    ];
    this.loading = false;
    console.log('✅ Offres mockées chargées:', this.offers.length);
  }

  // Méthode utilitaire pour formater le prix
  formatPrice(price: number): string {
    return `${price.toFixed(2)}€`;
  }

  // Méthode utilitaire pour formater la date
  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}