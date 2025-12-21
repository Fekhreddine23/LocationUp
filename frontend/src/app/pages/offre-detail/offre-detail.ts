import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { Offer } from '../../core/models/offer.model';
import { OffersService } from '../../core/services/offers.service';
import { BookingsService, Booking } from '../../core/services/bookings';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService as ToastNotificationService } from '../../core/services/notification.service';
import { NotificationService as NotificationStreamService } from '../../core/services/notification/notification';
import { Notification as NotificationPayload } from '../../core/models/notification/notification.model';
import { Toast } from '../../core/models/toast.model';
import { BRAND_IMAGE_MAP, BrandImageKey, findBrandKey } from '../../core/config/brand-images';

interface HighlightBadge {
  icon: string;
  label: string;
}

@Component({
  selector: 'app-offre-detail',
  standalone: true,
  imports: [CommonModule, CurrencyPipe],
  templateUrl: './offre-detail.html',
  styleUrl: './offre-detail.scss'
})
export class OffreDetail implements OnInit, OnDestroy {
  offer: Offer | null = null;
  isLoading = false;
  errorMessage = '';
  galleryImages: string[] = [];
  selectedImage: string | null = null;
  equipmentList: string[] = [];
  userBookingsForOffer: Booking[] = [];
  selectedBookingForRetry: Booking | null = null;
  bookingsLoading = false;
  isAuthenticated = false;
  highlightBadges: HighlightBadge[] = [];
  coverageLabel = '';
  cancellationLabel = '';
  addonsList: string[] = [];
  assuranceConditions: string[] = [];
  isFavorite = false;
  favoriteLoading = false;

  private authSub?: Subscription;
  private bookingsSub?: Subscription;
  private favoritesSub?: Subscription;
  private notificationStreamSub?: Subscription;
  private currentNotificationUserId?: string;
  private readonly retryStatuses = ['PENDING', 'REQUIRES_ACTION', 'FAILED', 'EXPIRED'];
  private galleryTouchStartX: number | null = null;
  private galleryTouchCurrentX: number | null = null;
  private readonly swipeThreshold = 40;

  constructor(
    private offersService: OffersService,
    private route: ActivatedRoute,
    private router: Router,
    private bookingsService: BookingsService,
    private authService: AuthService,
    private toastService: ToastNotificationService,
    private notificationStreamService: NotificationStreamService
  ) {
    this.isAuthenticated = this.authService.isLoggedIn();
    this.authSub = this.authService.currentUser.subscribe(user => {
      const wasAuthenticated = this.isAuthenticated;
      this.isAuthenticated = !!user;
      if (!this.isAuthenticated) {
        this.resetBookingContext();
        this.tearDownNotificationStream();
      } else {
        this.initNotificationStream();
        if (this.offer && !wasAuthenticated) {
          this.fetchUserBookingsForOffer(this.offer.offerId);
        }
      }
    });

    this.favoritesSub = this.offersService.getFavoriteIdsStream().subscribe(ids => {
      if (this.offer) {
        this.isFavorite = ids.includes(this.offer.offerId);
      }
    });

    if (this.isAuthenticated) {
      this.initNotificationStream();
    }
  }

  ngOnInit(): void {
    this.loadOfferDetails();
  }

  ngOnDestroy(): void {
    this.authSub?.unsubscribe();
    this.bookingsSub?.unsubscribe();
    this.favoritesSub?.unsubscribe();
    this.notificationStreamSub?.unsubscribe();
  }

  private loadOfferDetails(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.route.params.subscribe(params => {
      const offerId = Number(params['id']);

      if (offerId > 0) {
        this.offersService.getOfferById(offerId).subscribe({
          next: (offer) => {
            this.offer = offer;
            this.galleryImages = this.buildGalleryForOffer(offer);
            this.selectedImage = this.galleryImages[0] ?? offer.imageUrl ?? '';
            this.equipmentList = this.extractEquipmentList(offer);
            this.isFavorite = this.offersService.isFavorite(offer.offerId);
            this.updateContextualInsights();
            if (this.isAuthenticated) {
              this.fetchUserBookingsForOffer(offer.offerId);
            } else {
              this.resetBookingContext();
            }
            this.isLoading = false;
          },
          error: (error) => {
            this.isLoading = false;
            this.errorMessage = 'Erreur lors du chargement des détails de l\'offre';
            console.error('❌ Error loading offer details:', error);
          }
        });
      } else {
        this.errorMessage = 'ID d\'offre invalide';
        this.isLoading = false;
      }
    });
  }

  private resetBookingContext(): void {
    this.bookingsSub?.unsubscribe();
    this.bookingsSub = undefined;
    this.userBookingsForOffer = [];
    this.selectedBookingForRetry = null;
    this.bookingsLoading = false;
  }

  formatDate(dateString: string): string {
    if (!dateString) {
      return 'Non communiqué';
    }
    return new Date(dateString).toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  isOfferAvailable(): boolean {
    return this.offer ? this.offersService.isOfferAvailable(this.offer) : false;
  }

  reserveOffer(): void {
    if (this.offer) {
      this.router.navigate(['/bookings/new'], {
        queryParams: { offerId: this.offer.offerId }
      });
    }
  }

  selectImage(image: string): void {
    this.selectedImage = image;
  }

  toggleFavorite(): void {
    if (!this.offer || this.favoriteLoading) {
      return;
    }
    this.favoriteLoading = true;
    this.offersService.toggleFavorite(this.offer).subscribe({
      next: (favorite) => {
        this.isFavorite = favorite;
        this.favoriteLoading = false;
      },
      error: (error) => {
        console.error('Impossible de mettre à jour le favori', error);
        this.favoriteLoading = false;
      }
    });
  }

  redirectToLogin(): void {
    const redirect = this.router.url || `/offers/${this.offer?.offerId ?? ''}`;
    this.router.navigate(['/login'], { queryParams: { redirect } })
      .catch(err => console.warn('Navigation login impossible', err));
  }

  onGalleryTouchStart(event: TouchEvent): void {
    if (event.touches.length > 1) {
      return;
    }
    this.galleryTouchStartX = event.touches[0].clientX;
    this.galleryTouchCurrentX = event.touches[0].clientX;
  }

  onGalleryTouchMove(event: TouchEvent): void {
    if (this.galleryTouchStartX === null) {
      return;
    }
    this.galleryTouchCurrentX = event.touches[0].clientX;
  }

  onGalleryTouchEnd(): void {
    if (this.galleryTouchStartX === null || this.galleryTouchCurrentX === null) {
      this.resetSwipeTracking();
      return;
    }
    const delta = this.galleryTouchCurrentX - this.galleryTouchStartX;
    if (Math.abs(delta) > this.swipeThreshold) {
      if (delta < 0) {
        this.goToNextImage();
      } else {
        this.goToPreviousImage();
      }
    }
    this.resetSwipeTracking();
  }

  private resetSwipeTracking(): void {
    this.galleryTouchStartX = null;
    this.galleryTouchCurrentX = null;
  }

  goToNextImage(): void {
    this.goToImageOffset(1);
  }

  goToPreviousImage(): void {
    this.goToImageOffset(-1);
  }

  private goToImageOffset(offset: number): void {
    if (!this.galleryImages.length) {
      return;
    }
    const currentIndex = this.galleryImages.indexOf(this.selectedImage ?? this.galleryImages[0]);
    const baseIndex = currentIndex >= 0 ? currentIndex : 0;
    const nextIndex = (baseIndex + offset + this.galleryImages.length) % this.galleryImages.length;
    this.selectedImage = this.galleryImages[nextIndex];
  }

  shouldShowRetryPayment(): boolean {
    return this.isAuthenticated && this.userBookingsForOffer.length > 0;
  }

  selectRetryBooking(booking: Booking): void {
    this.selectedBookingForRetry = booking;
    this.updateContextualInsights();
  }

  retryPayment(): void {
    if (this.selectedBookingForRetry?.reservationId) {
      this.router.navigate(['/payments/retry'], {
        queryParams: { reservationId: this.selectedBookingForRetry.reservationId }
      }).catch(err => console.warn('Navigation retry paiement impossible', err));
      return;
    }
    if (this.offer) {
      this.router.navigate(['/payments/retry'], {
        queryParams: { offerId: this.offer.offerId }
      }).catch(err => console.warn('Navigation retry paiement impossible', err));
    }
  }

  contactSupport(): void {
    if (!this.offer) return;
    const pickup = this.getPickupLabel();
    const pickupDate = this.formatDate(this.offer.pickupDatetime);
    const subject = `Assistance offre #${this.offer.offerId} – ${pickup}`;
    const message = `Bonjour, je souhaite de l'aide pour l'offre #${this.offer.offerId} (${pickup}) prévue le ${pickupDate}.`;
    this.router.navigate(['/support'], {
      queryParams: {
        subject,
        offerId: this.offer.offerId,
        message
      }
    }).catch(err => console.warn('Navigation support impossible', err));
  }

  goBack(): void {
    this.router.navigate(['/offers']);
  }

  private buildGalleryForOffer(offer: Offer): string[] {
    const fromOffer = offer.galleryUrls ?? [];
    const brandImages = this.getBrandImages(offer);
    const combined = [...fromOffer, ...brandImages].filter(Boolean);
    if (combined.length) {
      return Array.from(new Set(combined));
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

  private resolveImage(offer: Offer): string {
    const brandImages = this.getBrandImages(offer);
    if (brandImages.length) {
      return brandImages[0];
    }
    if (offer?.imageUrl) {
      return offer.imageUrl;
    }
    return 'https://images.unsplash.com/photo-1477847616630-cf9cf8815fda?auto=format&fit=crop&w=900&q=80';
  }

  private getGalleryKeyword(offer: Offer): string {
    const brand = this.getBrandKey(offer);
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
    if (text.includes('voiture')) return 'car';
    if (text.includes('scooter')) return 'scooter';
    if (text.includes('velo') || text.includes('vélo')) return 'bike';
    if (text.includes('trottinette')) return 'scooter';
    if (text.includes('cargo') || text.includes('utilitaire')) return 'van';
    return 'transport';
  }

  private getBrandImages(offer: Offer): string[] {
    const brandKey = this.getBrandKey(offer);
    if (!brandKey) {
      return [];
    }
    const meta = BRAND_IMAGE_MAP[brandKey];
    if (!meta) {
      return [];
    }
    const images = new Set<string>();
    if (meta.card) {
      images.add(meta.card);
    }
    meta.gallery.forEach(img => images.add(img));
    return Array.from(images);
  }

  private getBrandKey(offer: Offer): BrandImageKey | null {
    const text = `${offer.description ?? ''} ${offer.mobilityService ?? ''}`;
    return findBrandKey(text);
  }

  private extractEquipmentList(offer: Offer): string[] {
    const features = new Set<string>([
      'Assurance incluse',
      'Support 24/7',
      'Annulation flexible'
    ]);
    const description = (offer.description || '').toLowerCase();
    if (description.includes('gps')) {
      features.add('GPS intégré');
    }
    if (description.includes('clim')) {
      features.add('Climatisation');
    }
    if (description.includes('électrique') || description.includes('batterie')) {
      features.add('Batterie rechargée');
    }
    return Array.from(features);
  }

  private updateContextualInsights(): void {
    if (!this.offer) {
      this.highlightBadges = [];
      this.coverageLabel = '';
      this.cancellationLabel = '';
      this.addonsList = [];
      this.assuranceConditions = [];
      return;
    }
    const booking = this.selectedBookingForRetry ?? this.userBookingsForOffer[0];
    const coverage = this.buildCoverageLabel(this.offer);
    const cancellation = this.buildCancellationPolicy(this.offer, booking ?? undefined);
    this.coverageLabel = coverage;
    this.cancellationLabel = cancellation;
    this.addonsList = this.buildAddonsList(this.offer);
    this.highlightBadges = this.buildHighlightBadges(this.offer, booking ?? undefined);
    this.assuranceConditions = this.buildAssuranceConditions(this.offer, booking ?? undefined, coverage, cancellation);
  }

  private buildCoverageLabel(offer: Offer): string {
    const description = (offer.description || '').toLowerCase();
    if (description.includes('électrique')) {
      return 'Assurance Omnium + assistance recharge incluse';
    }
    if (description.includes('utilitaire') || description.includes('cargo')) {
      return 'Assurance marchandises + assistance 24/7 incluse';
    }
    if (description.includes('premium') || description.includes('haut de gamme')) {
      return 'Couverture premium avec rachat partiel de franchise';
    }
    return 'Couverture standard + assistance 24/7 incluses';
  }

  private buildCancellationPolicy(offer: Offer, booking?: Booking): string {
    if (booking?.status === 'CONFIRMED') {
      return 'Modification possible depuis votre espace client. Contactez-nous pour ajuster l’horaire.';
    }
    return 'Annulation gratuite jusqu’à 24h avant le départ puis frais transparents.';
  }

  private buildAddonsList(offer: Offer): string[] {
    const addons: string[] = ['Check-in digital et état des lieux partagé'];
    const text = (offer.description || '').toLowerCase();
    if (text.includes('gps')) {
      addons.push('Pack GPS connecté inclus');
    }
    if (text.includes('clim') || text.includes('climatisation')) {
      addons.push('Confort thermique certifié (clim/chauffage)');
    }
    if (text.includes('trottinette') || text.includes('scooter')) {
      addons.push('Casque homologué + antivol offerts');
    }
    if (text.includes('cargo')) {
      addons.push('Kit sangles & couverture fourni');
    }
    return Array.from(new Set(addons));
  }

  private buildHighlightBadges(offer: Offer, booking?: Booking): HighlightBadge[] {
    const badges: HighlightBadge[] = [];
    const text = (offer.description || '').toLowerCase();
    const available = this.offersService.isOfferAvailable(offer);
    badges.push({
      icon: available ? '✅' : '⏱️',
      label: available ? 'Offre disponible' : 'Indisponible pour le moment'
    });
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
      badges.push({ icon: '🔁', label: `Réservation ${this.getRetryStatusLabel(booking).toLowerCase()}` });
    }
    return Array.from(new Map(badges.map(b => [b.icon + b.label, b])).values());
  }

  private buildAssuranceConditions(
    offer: Offer,
    booking?: Booking,
    coverageLabel?: string,
    cancellationLabel?: string
  ): string[] {
    const labels = new Set<string>();
    if (coverageLabel) {
      labels.add(coverageLabel);
    }
    if (cancellationLabel) {
      labels.add(cancellationLabel);
    }
    if (offer.status) {
      labels.add(`Statut administratif : ${offer.status}`);
    }
    if (booking) {
      labels.add(`Réservation : ${this.getRetryStatusLabel(booking)}`);
    }
    const equipment = this.equipmentList.length ? this.equipmentList : this.extractEquipmentList(offer);
    equipment.filter(item => /assurance|assistance|couverture|annulation/i.test(item))
      .forEach(item => labels.add(item));
    return Array.from(labels);
  }

  private initNotificationStream(): void {
    const userId = this.resolveCurrentUserId();
    if (!userId) {
      this.tearDownNotificationStream();
      return;
    }
    if (this.notificationStreamSub && this.currentNotificationUserId === userId) {
      return;
    }
    this.tearDownNotificationStream();
    this.currentNotificationUserId = userId;
    this.notificationStreamSub = this.notificationStreamService.connect(userId).subscribe({
      next: notification => this.handleNotificationToast(notification),
      error: error => console.error('Flux notifications offre détail', error)
    });
  }

  private tearDownNotificationStream(): void {
    this.notificationStreamSub?.unsubscribe();
    this.notificationStreamSub = undefined;
    this.currentNotificationUserId = undefined;
  }

  private handleNotificationToast(notification: NotificationPayload): void {
    const toastData = this.mapNotificationToToast(notification);
    if (toastData) {
      this.toastService.show(toastData.message, toastData.type);
    }
  }

  private mapNotificationToToast(notification: NotificationPayload): { message: string; type: Toast['type'] } | null {
    if (!notification) {
      return null;
    }
    const metadata = notification.metadata ?? {};
    const eventTypeRaw = (metadata['eventType'] ?? notification.title ?? notification.category ?? '').toString();
    const normalizedEvent = eventTypeRaw.toUpperCase();
    const offerId = this.parseNumber(metadata['offerId'] ?? metadata['offerID']);
    const reservationId = this.parseNumber(metadata['reservationId']);
    const paymentStatus = typeof metadata['paymentStatus'] === 'string'
      ? metadata['paymentStatus'].toUpperCase()
      : undefined;
    const offerLabel =
      (typeof metadata['offerTitle'] === 'string' && metadata['offerTitle'].trim().length > 0)
        ? metadata['offerTitle']
        : (this.offer?.description ?? `Offre #${offerId ?? this.offer?.offerId ?? '...'}`);
    const currentOfferMatch = offerId != null && this.offer?.offerId === offerId;

    if (normalizedEvent.includes('OFFER_FAVORITE') || normalizedEvent.includes('FAVORITE_ADDED')) {
      const pathHint = `/offers/${offerId ?? this.offer?.offerId ?? ''}`;
      const localMessage = currentOfferMatch
        ? 'Cette fiche est maintenant dans vos favoris.'
        : `"${offerLabel}" a été ajouté à vos favoris.`;
      return {
        message: `⭐ ${localMessage} → ${pathHint}`,
        type: 'success'
      };
    }

    if (normalizedEvent.includes('FAVORITE_REMOVED')) {
      const pathHint = offerId ? `/offers/${offerId}` : '/offers';
      const localMessage = currentOfferMatch
        ? 'Cette fiche a été retirée de vos favoris.'
        : `"${offerLabel}" a été retirée de vos favoris.`;
      return {
        message: `👋 ${localMessage} → ${pathHint}`,
        type: 'info'
      };
    }

    const isPaymentEvent = normalizedEvent.includes('PAYMENT') || paymentStatus != null;
    if (isPaymentEvent) {
      const label = reservationId ? `Réservation #${reservationId}` : 'Votre réservation';
      const retryEvent = normalizedEvent.includes('RETRY') || normalizedEvent.includes('PENDING');
      const baseRoute = reservationId ? `/payments/retry?reservationId=${reservationId}` : '/payments/retry';
      let type: Toast['type'] = 'info';
      if (normalizedEvent.includes('FAILED') || paymentStatus === 'FAILED') {
        type = 'error';
      } else if (normalizedEvent.includes('EXPIRED') || paymentStatus === 'EXPIRED') {
        type = 'warning';
      } else if (normalizedEvent.includes('SUCCESS')) {
        type = 'success';
      } else if (normalizedEvent.includes('RETRY')) {
        type = 'warning';
      }
      const prefix = retryEvent ? '🔁 Relance disponible' : '💳 Mise à jour paiement';
      return {
        message: `${prefix} pour ${label}. Accès rapide : ${baseRoute}`,
        type
      };
    }

    return null;
  }

  private parseNumber(value: unknown): number | undefined {
    if (value === null || value === undefined) {
      return undefined;
    }
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : undefined;
  }

  private resolveCurrentUserId(): string | undefined {
    const current = this.authService.currentUserValue;
    if (current?.id !== undefined && current?.id !== null) {
      return String(current.id);
    }
    try {
      if (typeof localStorage === 'undefined') {
        return undefined;
      }
      const stored = localStorage.getItem('currentUser');
      if (stored) {
        const parsed = JSON.parse(stored);
        if (parsed?.id !== undefined && parsed?.id !== null) {
          return String(parsed.id);
        }
      }
    } catch (error) {
      console.warn('Impossible de retrouver l’utilisateur courant depuis le stockage', error);
    }
    return undefined;
  }

  private fetchUserBookingsForOffer(offerId: number): void {
    this.bookingsSub?.unsubscribe();
    this.bookingsLoading = true;
    this.bookingsSub = this.bookingsService.getMyBookings().subscribe({
      next: bookings => {
        this.bookingsLoading = false;
        const eligible = bookings.filter(b => (b.offerId === offerId) && this.isBookingEligibleForRetry(b));
        this.userBookingsForOffer = eligible;
        this.selectedBookingForRetry = eligible[0] || null;
        this.updateContextualInsights();
      },
      error: (error) => {
        console.error('Error loading bookings context', error);
        this.bookingsLoading = false;
        this.userBookingsForOffer = [];
        this.selectedBookingForRetry = null;
        this.updateContextualInsights();
      }
    });
  }

  private isBookingEligibleForRetry(booking: Booking): boolean {
    const status = (booking.paymentStatus || booking.status || 'PENDING').toUpperCase();
    return this.retryStatuses.includes(status);
  }

  getPickupLabel(): string {
    if (!this.offer) return 'Lieu non précisé';
    return this.offer.pickupLocationName
      || this.offer.pickupLocationCity
      || this.offer.pickupLocation
      || `Lieu #${this.offer.pickupLocationId ?? 'N/A'}`;
  }

  getReturnLabel(): string {
    if (!this.offer) return 'Lieu non précisé';
    return this.offer.returnLocationName
      || this.offer.returnLocationCity
      || this.offer.returnLocation
      || `Lieu #${this.offer.returnLocationId ?? 'N/A'}`;
  }

  getMobilityServiceLabel(): string {
    if (!this.offer) return 'Service non précisé';
    return this.offer.mobilityServiceName
      || this.offer.mobilityService
      || `Service #${this.offer.mobilityServiceId ?? 'N/A'}`;
  }

  getRetryStatusLabel(booking?: Booking | null): string {
    if (!booking) return '';
    const status = (booking.paymentStatus || booking.status || 'PENDING').toUpperCase();
    const map: Record<string, string> = {
      PENDING: 'Paiement en attente',
      REQUIRES_ACTION: 'Action requise',
      FAILED: 'Paiement échoué',
      EXPIRED: 'Paiement expiré'
    };
    return map[status] || status;
  }
}
