import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { OffersService } from '../../core/services/offers.service';
import { OfferCardComponent } from '../../components/offer-card/offer-card';
import { Offer } from '../../core/models/offer.model';
import { Breadcrumbs } from "../../components/breadcrumbs/breadcrumbs";
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { BookingsService, Booking } from '../../core/services/bookings';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';
import { IdentityService } from '../../core/services/identity.service';
import { IdentityStatus } from '../../core/models/identity.model';
import { findBrandKey } from '../../core/config/brand-images';

interface QuickViewHighlight {
  icon: string;
  label: string;
}

interface SupportPreset {
  subject: string;
  message: string;
}

interface QuickViewInsights {
  durationLabel: string;
  coverageLabel: string;
  cancellationLabel: string;
  addons: string[];
  highlightBadges: QuickViewHighlight[];
  deepDive: string[];
  supportPreset: SupportPreset;
  userBooking?: Booking;
}


@Component({
  selector: 'app-offers',
  standalone: true,
  imports: [CommonModule, OfferCardComponent, Breadcrumbs, ReactiveFormsModule], // ← Importer OfferCardComponent
  templateUrl: './offers.component.html',
  styleUrl: './offers.component.scss'
})
export class OffersComponent implements OnInit, OnDestroy {
  offers: Offer[] = [];
  filteredOffers: Offer[] = [];
  isLoading = false;
  errorMessage = '';
  quickViewOffer: Offer | null = null;
  filterForm: FormGroup;
  servicesOptions: string[] = [];
  locationsOptions: string[] = [];
  private subscriptions = new Subscription();
  private favoriteIds = new Set<number>();
  quickViewGallery: string[] = [];
  quickViewSelectedImage: string | null = null;
  quickViewEquipment: string[] = [];
  quickViewInsights: QuickViewInsights | null = null;
  private userBookingsByOffer = new Map<number, Booking>();
  private hasLoadedBookingsOnce = false;
  private bookingsLoading = false;
  isUserAuthenticated = false;
  identityStatus: IdentityStatus | null = null;
  identityLoading = false;

  constructor(
    public offersService: OffersService,
    private router: Router,
    private fb: FormBuilder,
    private bookingsService: BookingsService,
    private authService: AuthService,
    private notificationService: NotificationService,
    private identityService: IdentityService
  ) {
    this.filterForm = this.fb.group({
      search: [''],
      service: [''],
      location: [''],
      startDate: [''],
      minPrice: [null],
      maxPrice: [null],
      sort: ['recent'],
      onlyAvailable: [true],
      favoritesOnly: [false]
    });
    this.isUserAuthenticated = this.authService.isLoggedIn();
  }

  ngOnInit(): void {
    this.loadOffers();
    this.subscriptions.add(
      this.filterForm.valueChanges.subscribe(() => this.applyFilters())
    );
    this.subscriptions.add(
      this.offersService.getFavoriteIdsStream().subscribe(ids => {
        this.favoriteIds = new Set(ids);
        this.applyFilters(false);
      })
    );
    this.subscriptions.add(
      this.authService.currentUser.subscribe(user => {
        const wasAuthenticated = this.isUserAuthenticated;
        this.isUserAuthenticated = !!user;
        if (!this.isUserAuthenticated) {
          this.userBookingsByOffer.clear();
          this.hasLoadedBookingsOnce = false;
          this.identityStatus = null;
        } else {
          if (!wasAuthenticated || !this.hasLoadedBookingsOnce) {
            this.loadUserBookingsContext();
          }
          this.loadIdentityStatus();
        }
      })
    );
    if (this.isUserAuthenticated) {
      this.loadUserBookingsContext();
      this.loadIdentityStatus();
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  loadOffers(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.offersService.getAllOffers().subscribe({
      next: (offers) => {
        this.offers = offers;
        this.buildFilterOptions();
        this.applyFilters();
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

  private buildFilterOptions(): void {
    const services = new Set<string>();
    const locations = new Set<string>();

    this.offers.forEach(offer => {
      if (offer.mobilityService) {
        services.add(offer.mobilityService);
      }
      const locationLabel = this.getLocationLabel(offer);
      if (locationLabel) {
        locations.add(locationLabel);
      }
    });

    this.servicesOptions = Array.from(services).sort();
    this.locationsOptions = Array.from(locations).sort();
  }

  applyFilters(resetSelection: boolean = true): void {
    if (!this.offers?.length) {
      this.filteredOffers = [];
      return;
    }

    const filters = this.filterForm.value;
    let results = [...this.offers];

    if (filters.search) {
      const search = filters.search.toLowerCase();
      results = results.filter(offer =>
        (offer.description || '').toLowerCase().includes(search) ||
        this.getLocationLabel(offer).toLowerCase().includes(search) ||
        (offer.mobilityService || '').toLowerCase().includes(search)
      );
    }

    if (filters.service) {
      results = results.filter(o => o.mobilityService === filters.service);
    }

    if (filters.location) {
      results = results.filter(o => this.getLocationLabel(o) === filters.location);
    }

    if (filters.startDate) {
      const fromDate = new Date(filters.startDate);
      results = results.filter(o => new Date(o.pickupDatetime) >= fromDate);
    }

    if (filters.minPrice != null) {
      results = results.filter(o => o.price >= filters.minPrice);
    }

    if (filters.maxPrice != null) {
      results = results.filter(o => o.price <= filters.maxPrice);
    }

    if (filters.onlyAvailable) {
      results = results.filter(o => this.offersService.isOfferAvailable(o));
    }

    if (filters.favoritesOnly) {
      results = results.filter(o => this.favoriteIds.has(o.offerId));
    }

    switch (filters.sort) {
      case 'priceAsc':
        results.sort((a, b) => a.price - b.price);
        break;
      case 'priceDesc':
        results.sort((a, b) => b.price - a.price);
        break;
      default:
        results.sort((a, b) => new Date(a.pickupDatetime).getTime() - new Date(b.pickupDatetime).getTime());
        break;
    }

    this.filteredOffers = results;

    if (resetSelection && this.quickViewOffer) {
      const stillVisible = results.find(o => o.offerId === this.quickViewOffer?.offerId);
      if (!stillVisible) {
        this.quickViewOffer = null;
      }
    }
  }

  getLocationLabel(offer: Offer): string {
    return (
      offer.pickupLocationName ||
      offer.pickupLocation ||
      offer.pickupLocationCity ||
      `Lieu #${offer.pickupLocationId ?? offer.offerId}`
    );
  }

  resetFilters(): void {
    this.filterForm.reset({
      search: '',
      service: '',
      location: '',
      startDate: '',
      minPrice: null,
      maxPrice: null,
      sort: 'recent',
      onlyAvailable: true,
      favoritesOnly: false
    });
  }

  toggleFavorite(offer: Offer): void {
    this.offersService.toggleFavorite(offer).subscribe({
      error: (error) => console.error('Impossible de mettre à jour le favori', error)
    });
  }

  isFavorite(offerId: number): boolean {
    return this.favoriteIds.has(offerId);
  }

  toggleQuickView(offer: Offer): void {
    if (this.quickViewOffer?.offerId === offer.offerId) {
      this.quickViewOffer = null;
      this.quickViewGallery = [];
      this.quickViewSelectedImage = null;
      this.quickViewEquipment = [];
      this.quickViewInsights = null;
    } else {
      this.quickViewOffer = offer;
      this.quickViewGallery = this.buildGalleryForOffer(offer);
      this.quickViewSelectedImage = this.quickViewGallery[0] ?? this.resolveImage(offer);
      this.quickViewEquipment = this.extractEquipmentList(offer);
      this.quickViewInsights = this.buildQuickViewInsights(offer);
    }
  }

  closeQuickView(): void {
    this.quickViewOffer = null;
    this.quickViewGallery = [];
    this.quickViewSelectedImage = null;
    this.quickViewEquipment = [];
    this.quickViewInsights = null;
  }

  isQuickViewOpenFor(offer: Offer): boolean {
    return this.quickViewOffer?.offerId === offer.offerId;
  }

  getAvailabilityLabel(offer: Offer): string {
    const now = new Date();
    if (!this.offersService.isOfferAvailable(offer)) {
      const status = (offer.status ?? '').toString().toUpperCase();
      if (status === 'CANCELLED') {
        return 'Annulée';
      }
      if (status === 'COMPLETED') {
        return 'Clôturée';
      }
      return 'Indisponible';
    }
    const pickupDate = offer.pickupDatetime ? new Date(offer.pickupDatetime) : null;
    const hasPastPickupDate = !!pickupDate && !Number.isNaN(pickupDate.getTime()) && pickupDate < now;
    return hasPastPickupDate ? 'Disponible (créneau à définir)' : 'Disponible';
  }

  trackByOffer(_: number, offer: Offer): number {
    return offer.offerId;
  }

  formatPrice(value: number): string {
    return this.offersService.formatPrice(value);
  }

  formatDate(value: string): string {
    return this.offersService.formatDateForDisplay(value);
  }

  resolveImage(offer?: Offer | null): string {
    return this.offersService.resolveOfferImage(offer);
  }

  reserveOffer(offer?: Offer | null): void {
    if (!offer) {
      return;
    }
    if (this.isUserAuthenticated && !this.isIdentityVerified) {
      this.notificationService.warning('Vérifiez votre identité pour poursuivre la réservation.');
      this.router.navigate(['/profile'], { fragment: 'identity' });
      return;
    }
    this.router.navigate(['/bookings/new'], {
      queryParams: { offerId: offer.offerId }
    });
  }

  selectGalleryImage(image: string): void {
    this.quickViewSelectedImage = image;
  }

  shouldShowRetryPayment(offer: Offer): boolean {
    const booking = this.getUserBookingForOffer(offer.offerId);
    if (!booking) {
      return false;
    }
    const paymentStatus = booking.paymentStatus ?? booking.status ?? 'PENDING';
    return ['PENDING', 'REQUIRES_ACTION', 'FAILED', 'EXPIRED'].includes(paymentStatus);
  }

  retryPayment(offer: Offer): void {
    this.router.navigate(['/payments/retry'], {
      queryParams: { offerId: offer.offerId }
    }).catch(err => console.warn('Navigation retry paiement impossible', err));
  }

  contactSupport(offer: Offer, preset?: SupportPreset | null): void {
    this.router.navigate(['/support'], {
      queryParams: {
        subject: preset?.subject ?? `Aide pour l'offre #${offer.offerId}`,
        offerId: offer.offerId,
        message: preset?.message
      }
    }).catch(err => console.warn('Navigation support impossible', err));
  }

  navigateToBooking(booking: Booking): void {
    if (!booking?.reservationId) {
      return;
    }
    this.router.navigate(['/bookings', booking.reservationId]).catch(err =>
      console.warn('Navigation réservation impossible', err)
    );
  }

  get favoritesOnlyControl(): FormControl {
    return this.filterForm.get('favoritesOnly') as FormControl;
  }

  private buildGalleryForOffer(offer: Offer): string[] {
    const fromOffer = offer.galleryUrls ?? [];
    const brandImages = this.getBrandImages(offer);
    const gallery = [
      ...fromOffer,
      ...brandImages
    ].filter(Boolean);

    if (gallery.length) {
      return Array.from(new Set(gallery));
    }

    const base = this.resolveImage(offer);
    const keyword = this.getGalleryKeyword(offer);
    const candidates = [
      base,
      `https://source.unsplash.com/900x600/?${keyword},premium`,
      `https://source.unsplash.com/901x600/?${keyword},mobility`,
      `https://source.unsplash.com/902x600/?${keyword},detail`
    ];
    return Array.from(new Set(candidates.filter(Boolean)));
  }

  private getGalleryKeyword(offer: Offer): string {
    const brand = findBrandKey(`${offer.description ?? ''} ${offer.mobilityService ?? ''}`);
    if (brand) {
      return brand;
    }
    const text = `${offer.description ?? ''} ${offer.mobilityService ?? ''}`.toLowerCase();
    if (text.includes('clio') || text.includes('renault')) return 'clio';
    if (text.includes('peugeot')) return 'peugeot';
    if (text.includes('dacia')) return 'dacia';
    if (text.includes('toyota')) return 'toyota';
    if (text.includes('citroen')) return 'citroen';
    if (text.includes('nissan')) return 'nissan';
    if (text.includes('kangoo')) return 'kangoo';
    if (text.includes('tesla')) return 'tesla';
    if (text.includes('voiture') || text.includes('car')) return 'car';
    if (text.includes('scooter')) return 'scooter';
    if (text.includes('velo') || text.includes('vélo') || text.includes('bike')) return 'bike';
    if (text.includes('trottinette') || text.includes('trotinette')) return 'scooter';
    if (text.includes('cargo') || text.includes('utilitaire')) return 'van';
    return 'transport';
  }

  private getBrandImages(offer: Offer): string[] {
    return this.offersService.getBrandImages(offer);
  }

  private extractEquipmentList(offer: Offer): string[] {
    const base: string[] = [
      'Assistance 24/7',
      'Annulation flexible',
      'Assurance incluse'
    ];
    const description = (offer.description || '').toLowerCase();
    if (description.includes('gps')) {
      base.push('GPS intégré');
    }
    if (description.includes('clim') || description.includes('climatisation')) {
      base.push('Climatisation');
    }
    if (description.includes('charge') || description.includes('électrique')) {
      base.push('Charge rapide incluse');
    }
    return Array.from(new Set(base));
  }

  private loadUserBookingsContext(): void {
    if (this.bookingsLoading || !this.authService.isLoggedIn()) {
      return;
    }
    this.bookingsLoading = true;
    this.subscriptions.add(
      this.bookingsService.getMyBookings().subscribe({
        next: bookings => {
          this.bookingsLoading = false;
          this.hasLoadedBookingsOnce = true;
          this.userBookingsByOffer = new Map(
            bookings
              .filter(booking => typeof booking.offerId === 'number')
              .map(booking => [booking.offerId as number, booking])
          );
          if (this.quickViewOffer) {
            this.quickViewInsights = this.buildQuickViewInsights(this.quickViewOffer);
          }
        },
        error: err => {
          this.bookingsLoading = false;
          console.error('Impossible de charger les réservations utilisateur', err);
          this.notificationService.warning('Vos réservations n\'ont pas pu être chargées. Certaines actions peuvent être limitées.');
        }
      })
    );
  }

  private getUserBookingForOffer(offerId: number): Booking | undefined {
    return this.userBookingsByOffer.get(offerId);
  }

  private buildQuickViewInsights(offer: Offer): QuickViewInsights {
    const booking = this.getUserBookingForOffer(offer.offerId);
    return {
      durationLabel: this.buildDurationLabel(offer, booking),
      coverageLabel: this.buildCoverageLabel(offer),
      cancellationLabel: this.buildCancellationPolicy(offer, booking),
      addons: this.buildAddonsList(offer),
      highlightBadges: this.buildHighlightBadges(offer, booking),
      deepDive: this.buildDeepDiveList(offer),
      supportPreset: this.buildSupportPreset(offer, booking),
      userBooking: booking
    };
  }

  private buildDurationLabel(offer: Offer, booking?: Booking): string {
    const pickup = new Date(offer.pickupDatetime);
    const formattedDay = pickup.toLocaleDateString('fr-FR', { weekday: 'long', day: '2-digit', month: 'long' });
    const formattedTime = pickup.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
    const flex = booking ? 'créneau garanti' : 'flexibilité +/- 90 min';
    return `${formattedDay} à ${formattedTime} • ${flex}`;
  }

  private buildCoverageLabel(offer: Offer): string {
    const description = (offer.description || '').toLowerCase();
    if (description.includes('électrique')) {
      return 'Assurance Omnium + assistance recharge incluse';
    }
    if (description.includes('utilitaire') || description.includes('cargo')) {
      return 'Assurance marchandises + assistance 24/7 incluse';
    }
    return 'Couverture premium + assistance 24/7 incluses dans le forfait';
  }

  private buildCancellationPolicy(offer: Offer, booking?: Booking): string {
    if (booking && booking.status === 'CONFIRMED') {
      return 'Modification possible depuis votre espace client. Contactez-nous pour ajuster l\'horaire.';
    }
    return 'Annulation gratuite jusqu\'à 24h avant le départ. Frais transparents ensuite.';
  }

  private buildAddonsList(offer: Offer): string[] {
    const addons: string[] = ['Check-in digital avec photos partagées'];
    const text = (offer.description || '').toLowerCase();
    if (text.includes('gps')) {
      addons.push('Pack GPS connecté inclus');
    }
    if (text.includes('clim')) {
      addons.push('Confort thermique certifié (clim/chauffage)');
    }
    if (text.includes('scooter') || text.includes('trottinette')) {
      addons.push('Casque homologué + antivol offerts');
    }
    if (text.includes('cargo')) {
      addons.push('Kit sangles et couverture fourni');
    }
    return Array.from(new Set(addons));
  }

  private buildHighlightBadges(offer: Offer, booking?: Booking): QuickViewHighlight[] {
    const badges: QuickViewHighlight[] = [];
    const text = (offer.description || '').toLowerCase();
    const available = this.offersService.isOfferAvailable(offer);
    badges.push({ icon: available ? '✅' : '⏱️', label: available ? 'Immobilisation limitée' : 'Prochain créneau à confirmer' });
    if (text.includes('électrique')) {
      badges.push({ icon: '⚡️', label: 'Recharge comprise' });
    }
    if (text.includes('premium') || text.includes('haut de gamme')) {
      badges.push({ icon: '✨', label: 'Finition premium' });
    }
    if (text.includes('utilitaire') || text.includes('cargo')) {
      badges.push({ icon: '📦', label: 'Volume optimisé' });
    }
    if (booking) {
      const paymentStatus = booking.paymentStatus ?? booking.status;
      badges.push({ icon: '🔁', label: `Réservation ${paymentStatus?.toLowerCase() || 'en cours'}` });
    }
    return Array.from(new Map(badges.map(b => [b.icon + b.label, b])).values());
  }

  private buildDeepDiveList(offer: Offer): string[] {
    return [
      'Diagnostic photo envoyé avant remise des clés',
      'Checklist LocationUp partagée par email',
      `Point de retrait : ${this.getLocationLabel(offer)}`,
      'Support temps réel via chat + SMS'
    ];
  }

  private buildSupportPreset(offer: Offer, booking?: Booking): SupportPreset {
    if (booking?.reservationId) {
      return {
        subject: `Assistance réservation #${booking.reservationId}`,
        message: `Bonjour, j'ai besoin d'aide concernant ma réservation #${booking.reservationId} (offre #${offer.offerId}).`
      };
    }
    return {
      subject: `Question sur l'offre #${offer.offerId}`,
      message: `Bonjour, pourriez-vous me confirmer les disponibilités de l'offre #${offer.offerId} ?`
    };
  }
  private loadIdentityStatus(): void {
    if (!this.isUserAuthenticated) {
      this.identityStatus = null;
      return;
    }
    this.identityLoading = true;
    this.identityService.getStatus().subscribe({
      next: status => {
        this.identityStatus = status;
        this.identityLoading = false;
      },
      error: err => {
        console.warn('Impossible de charger le statut identité', err);
        this.identityLoading = false;
        this.identityStatus = null;
      }
    });
  }

  get isIdentityVerified(): boolean {
    return (this.identityStatus?.status?.toUpperCase() ?? '') === 'VERIFIED';
  }
}
