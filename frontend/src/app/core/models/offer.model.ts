export type OfferStatus = 'ACTIVE' | 'INACTIVE' | 'EXPIRED';

export interface Offer {
  offerId: number;
  description: string;
  price: number;
  pickupDatetime: string;
  createdAt: string;
  updatedAt: string;
  version: number; // ← Doit être présent et obligatoire
  adminId: number;
  mobilityServiceId: number;
  pickupLocationId: number;
  returnLocationId: number;
  status?: OfferStatus;
  mobilityService?: string;
  pickupLocation?: string;
  returnLocation?: string;
  adminName?: string;
}

export interface CreateOfferRequest {
  description: string;
  price: number;
  pickupDatetime: string;
  adminId: number;
  mobilityServiceId: number;
  pickupLocationId: number;
  returnLocationId: number;
  status?: OfferStatus;
  // version n'est pas requis pour la création
}
