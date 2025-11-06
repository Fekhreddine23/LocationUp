import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, tap } from 'rxjs';
import { environment } from '../../../environments/environment'; 
import { Offer, CreateOfferRequest } from '../../core/models/offer.model'; // ← Import séparé depuis le modèle
import { BusinessEventsService } from './business-events/business-events';
import { AuthService } from './auth.service';
@Injectable({
  providedIn: 'root'
})
export class OffersService {
  private apiUrl = '/api/offers';
  private useMocks = true; // ⚠️ Passez à false quand l'API backend sera prête

  constructor(private http: HttpClient, 
    private businessEvents: BusinessEventsService,
    private authService: AuthService // ✅ AJOUT 
  ) {}

  // GET /api/offers - Récupérer toutes les offres
  getAllOffers(): Observable<Offer[]> {
    if (this.useMocks) {
      return of(this.getMockOffers());
    }
    return this.http.get<Offer[]>(this.apiUrl);
  }

  // GET /api/offers/{id} - Récupérer une offre par ID
  getOfferById(id: number): Observable<Offer> {
    if (this.useMocks) {
      const offer = this.getMockOffers().find(o => o.offerId === id);
      if (!offer) {
        throw new Error(`Offer with ID ${id} not found`);
      }
      return of(offer);
    }
    return this.http.get<Offer>(`${this.apiUrl}/${id}`);
  }

  // POST /api/offers - Créer une nouvelle offre
  // ✅ MODIFIÉ : Créer une offre avec notification
  createOffer(offerData: CreateOfferRequest): Observable<Offer> {
    if (this.useMocks) {
      const newOffer: Offer = {
        ...offerData,
        offerId: Date.now(),
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        version: 0
      };

      // ✅ NOTIFICATION pour création d'offre
      const currentUser = this.authService.currentUserValue;
      if (currentUser) {
        this.businessEvents.notifyOfferCreated(
          newOffer.offerId,
          newOffer.description,
          currentUser.id
        ).subscribe();
      }
      
      return of(newOffer);
    }

    return this.http.post<Offer>(this.apiUrl, offerData).pipe(
      tap((newOffer: Offer) => {
        // ✅ NOTIFICATION pour création réelle
        const currentUser = this.authService.currentUserValue;
        if (currentUser) {
          this.businessEvents.notifyOfferCreated(
            newOffer.offerId,
            newOffer.description,
            currentUser.id
          ).subscribe();
        }
      })
    );
  }

  // PUT /api/offers/{id} - Mettre à jour une offre
  updateOffer(id: number, offerData: CreateOfferRequest): Observable<Offer> {
    if (this.useMocks) {
      const updatedOffer: Offer = {
        ...offerData,
        offerId: id,
        createdAt: new Date().toISOString(), // Mock value
        updatedAt: new Date().toISOString(),
        version: 1 // ← AJOUTÉ pour correspondre à l'interface
      };
      return of(updatedOffer);
    }
    return this.http.put<Offer>(`${this.apiUrl}/${id}`, offerData);
  }

  // DELETE /api/offers/{id} - Supprimer une offre
  deleteOffer(id: number): Observable<void> {
    if (this.useMocks) {
      console.log('Mock: Offer deleted with ID:', id);
      return of(void 0);
    }
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // 🔧 DONNÉES MOCK POUR LE DÉVELOPPEMENT - CORRIGÉES
  private getMockOffers(): Offer[] {
    return [
      {
        offerId: 1,
        description: 'Vélo électrique haut de gamme pour déplacements urbains. Autonomie 80km, charge rapide.',
        price: 18.50,
        pickupDatetime: new Date(Date.now() + 86400000).toISOString(), // Demain
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        version: 1, // ← AJOUTÉ
        adminId: 1,
        mobilityServiceId: 1,
        pickupLocationId: 1,
        returnLocationId: 1
      },
      {
        offerId: 2,
        description: 'Scooter 125cc récent, parfait pour la ville. Consommation réduite, confort optimal.',
        price: 35.00,
        pickupDatetime: new Date(Date.now() + 172800000).toISOString(), // Après-demain
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        version: 1, // ← AJOUTÉ
        adminId: 1,
        mobilityServiceId: 1,
        pickupLocationId: 2,
        returnLocationId: 2
      },
      {
        offerId: 3,
        description: 'Voiture citadine électrique. Idéale pour les familles, recharge incluse.',
        price: 65.00,
        pickupDatetime: new Date(Date.now() + 259200000).toISOString(), // 3 jours
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        version: 1, // ← AJOUTÉ
        adminId: 1,
        mobilityServiceId: 1,
        pickupLocationId: 1,
        returnLocationId: 1
      },
      {
        offerId: 4,
        description: 'Trottinette électrique pliable - Parfaite pour les courts trajets',
        price: 12.00,
        pickupDatetime: new Date(Date.now() + 432000000).toISOString(), // 5 jours
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        version: 1, // ← AJOUTÉ
        adminId: 1,
        mobilityServiceId: 1,
        pickupLocationId: 3,
        returnLocationId: 3
      },
      {
        offerId: 5,
        description: 'Vélo cargo électrique - Idéal pour les courses',
        price: 28.00,
        pickupDatetime: new Date(Date.now() + 604800000).toISOString(), // 7 jours
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        version: 1, // ← AJOUTÉ
        adminId: 1,
        mobilityServiceId: 1,
        pickupLocationId: 2,
        returnLocationId: 2
      }
    ];
  }

  // ALIAS pour compatibilité - utilisez getAllOffers() de préférence
  getOffers(): Observable<Offer[]> {
    return this.getAllOffers();
  }

  // Utilitaires
  formatDateForDisplay(isoDate: string): string {
    return new Date(isoDate).toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  formatPrice(price: number): string {
    return `${price.toFixed(2)}€`;
  }

  // Méthode pour basculer entre mocks et API réelle
  setUseMocks(useMocks: boolean): void {
    this.useMocks = useMocks;
    console.log(`🎯 OffersService: ${useMocks ? 'Using MOCKS' : 'Using REAL API'}`);
  }

  // Nouvelle méthode pour vérifier la disponibilité
  isOfferAvailable(offer: Offer): boolean {
    const now = new Date();
    const pickupDate = new Date(offer.pickupDatetime);
    return pickupDate > now;
  }

  // Filtrer les offres disponibles
  getAvailableOffers(): Observable<Offer[]> {
    return new Observable(observer => {
      this.getAllOffers().subscribe(offers => {
        const availableOffers = offers.filter(offer => this.isOfferAvailable(offer));
        observer.next(availableOffers);
        observer.complete();
      });
    });
  }


   // ✅ NOUVELLE MÉTHODE : Notifier nouvelle offre disponible
  notifyNewOfferAvailable(offer: Offer, targetUserId?: number) {
    if (targetUserId) {
      this.businessEvents.notifyOfferAvailable(
        offer.offerId,
        offer.description,
        targetUserId
      ).subscribe();
    }
  }
}


