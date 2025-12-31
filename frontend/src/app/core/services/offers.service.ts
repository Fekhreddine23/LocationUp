import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of, tap, catchError, map } from 'rxjs';
import { environment } from '../../../environments/environment'; 
import { Offer, CreateOfferRequest } from '../../core/models/offer.model'; // ← Import séparé depuis le modèle
import { BusinessEventsService } from './business-events/business-events';
import { AuthService } from './auth.service';
import { NotificationService } from './notification.service';
import { BRAND_IMAGE_MAP, BrandImageKey, findBrandKey } from '../config/brand-images';
@Injectable({
  providedIn: 'root'
})
export class OffersService {
  private apiUrl = `${environment.apiUrl}/api/offers`;
  private useMocks = environment.useMockOffers ?? false;
  private readonly favoritesStorageKey = 'locationup_favorite_offers';
  private favoriteIds = new Set<number>();
  private favoriteIds$ = new BehaviorSubject<number[]>([]);
  private favoriteOffers$ = new BehaviorSubject<Offer[]>([]);

  constructor(private http: HttpClient, 
              private businessEvents: BusinessEventsService,
              private authService: AuthService,
              private notificationService: NotificationService
  ) {
    this.authService.currentUser.subscribe(user => {
      if (user) {
        this.syncFavoritesFromApi();
      } else {
        this.restoreFavoritesFromStorage();
        this.favoriteOffers$.next([]);
      }
    });
  }

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
    const status = (offer.status ?? 'PENDING').toString().toUpperCase();
    if (['CANCELLED', 'COMPLETED'].includes(status)) {
      return false;
    }
    if (offer.active === false) {
      return false;
    }
    // Certaines offres remontent avec la date de création comme date de retrait :
    // on considère donc l'offre disponible tant qu'elle n'est pas clôturée.
    return true;
  }

  // Image principale d'une offre (marque → set d'assets → fallback imageUrl → unsplash)
  resolveOfferImage(offer?: Offer | null): string {
    if (!offer) {
      return this.defaultImage;
    }
    if (offer.galleryUrls?.length) {
      const first = offer.galleryUrls.find(Boolean);
      if (first) {
        return first;
      }
    }
    const brandImages = this.getBrandImages(offer);
    if (brandImages.length) {
      return brandImages[0];
    }
    return offer.imageUrl || this.defaultImage;
  }

  getBrandImages(offer: Offer): string[] {
    const brandKey = this.getBrandKey(offer);
    if (!brandKey) return [];
    const meta = BRAND_IMAGE_MAP[brandKey];
    if (!meta) return [];
    const ordered = new Set<string>();
    if (meta.card) ordered.add(meta.card);
    meta.gallery.forEach(img => ordered.add(img));
    return Array.from(ordered);
  }

  private getBrandKey(offer: Offer): BrandImageKey | null {
    const text = `${offer.description ?? ''} ${offer.mobilityService ?? ''}`;
    return findBrandKey(text);
  }

  private get defaultImage(): string {
    return 'https://images.unsplash.com/photo-1477847616630-cf9cf8815fda?auto=format&fit=crop&w=900&q=80';
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

  /**
   * ========= FAVORIS UTILISATEUR =========
   */
  private restoreFavoritesFromStorage(): void {
    try {
      const stored = typeof localStorage !== 'undefined' ? localStorage.getItem(this.favoritesStorageKey) : null;
      if (stored) {
        const parsed: number[] = JSON.parse(stored);
        this.updateFavoriteCache(parsed, false);
        return;
      }
    } catch (error) {
      console.warn('Impossible de restaurer les favoris locaux', error);
    }
    this.updateFavoriteCache([], false);
  }

  private syncFavoritesFromApi(): void {
    this.fetchFavoriteIdsFromApi();
    this.fetchFavoriteOffersFromApi();
  }

  private fetchFavoriteIdsFromApi(): void {
    this.http.get<number[]>(`${this.apiUrl}/favorites/ids`).pipe(
      catchError(error => {
        console.warn('Impossible de récupérer les favoris distants', error);
        this.restoreFavoritesFromStorage();
        return of(Array.from(this.favoriteIds));
      })
    ).subscribe(ids => {
      this.updateFavoriteCache(ids);
    });
  }

  private fetchFavoriteOffersFromApi(): void {
    this.getFavoriteOffers().subscribe();
  }

  private updateFavoriteCache(ids: number[], persistLocal: boolean = true): void {
    this.favoriteIds = new Set(ids);
    this.favoriteIds$.next(ids);
    if (persistLocal) {
      this.persistFavoritesLocal();
    }
  }

  getFavoriteOffers(): Observable<Offer[]> {
    if (!this.authService.isLoggedIn()) {
      return of([]);
    }
    return this.http.get<Offer[]>(`${this.apiUrl}/favorites`).pipe(
      tap(list => this.favoriteOffers$.next(list)),
      catchError(error => {
        console.warn('Impossible de récupérer la liste détaillée des favoris', error);
        this.favoriteOffers$.next([]);
        return of([]);
      })
    );
  }

  private persistFavoritesLocal(): void {
    if (typeof localStorage === 'undefined') {
      return;
    }
    const ids = Array.from(this.favoriteIds);
    localStorage.setItem(this.favoritesStorageKey, JSON.stringify(ids));
  }

  private applyFavoriteLocally(offerId: number, favorite: boolean): void {
    if (favorite) {
      this.favoriteIds.add(offerId);
    } else {
      this.favoriteIds.delete(offerId);
    }
    this.favoriteIds$.next(Array.from(this.favoriteIds));
  }

  private updateFavoriteOffersList(offer: Offer, favorite: boolean): void {
    const current = [...this.favoriteOffers$.value];
    const exists = current.find(o => o.offerId === offer.offerId);
    if (favorite) {
      if (!exists) {
        this.favoriteOffers$.next([...current, offer]);
      }
    } else if (exists) {
      this.favoriteOffers$.next(current.filter(o => o.offerId !== offer.offerId));
    }
  }

  private emitFavoriteBusinessEvent(offer: Offer): void {
    const currentUser = this.authService.currentUserValue;
    if (!currentUser) {
      return;
    }
    this.businessEvents.notifySystemEvent(
      'OFFER_FAVORITE',
      `L'utilisateur ${currentUser.username ?? currentUser.email ?? currentUser.id} suit "${offer.description?.substring(0, 40) ?? 'offre'}"`,
      'INFO',
      currentUser.id
    ).subscribe({
      error: (err) => console.warn('Impossible de notifier le favori', err)
    });
  }

  getFavoriteIdsStream(): Observable<number[]> {
    return this.favoriteIds$.asObservable();
  }

  getFavoriteOffersStream(): Observable<Offer[]> {
    return this.favoriteOffers$.asObservable();
  }

  refreshServerFavorites(): void {
    if (!this.authService.isLoggedIn()) {
      return;
    }
    this.syncFavoritesFromApi();
  }

  isFavorite(offerId: number): boolean {
    return this.favoriteIds.has(offerId);
  }

  toggleFavorite(offer: Offer): Observable<boolean> {
    const id = offer.offerId;
    const shouldFavorite = !this.favoriteIds.has(id);

    if (!this.authService.isLoggedIn()) {
      this.applyFavoriteLocally(id, shouldFavorite);
      this.updateFavoriteOffersList(offer, shouldFavorite);
      this.persistFavoritesLocal();
      if (shouldFavorite) {
        this.emitFavoriteBusinessEvent(offer);
      }
      this.displayFavoriteToast(offer, shouldFavorite);
      return of(shouldFavorite);
    }

    const request$ = shouldFavorite
      ? this.http.post<void>(`${this.apiUrl}/${id}/favorite`, {})
      : this.http.delete<void>(`${this.apiUrl}/${id}/favorite`);

    return request$.pipe(
      tap(() => {
        this.applyFavoriteLocally(id, shouldFavorite);
        this.updateFavoriteOffersList(offer, shouldFavorite);
        this.persistFavoritesLocal();
        if (shouldFavorite) {
          this.emitFavoriteBusinessEvent(offer);
        }
        this.displayFavoriteToast(offer, shouldFavorite);
      }),
      map(() => shouldFavorite),
      catchError(error => {
        console.error('Impossible de synchroniser le favori', error);
        return of(this.favoriteIds.has(id));
      })
    );
  }

  private displayFavoriteToast(offer: Offer, added: boolean): void {
    const label = offer.description?.split('\n')[0] ?? `Offre #${offer.offerId}`;
    if (added) {
      this.notificationService.success(`"${label}" a été ajoutée à vos favoris`);
    } else {
      this.notificationService.info(`"${label}" a été retirée de vos favoris`);
    }
  }
}
