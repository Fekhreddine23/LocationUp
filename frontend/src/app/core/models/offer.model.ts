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
}

export interface CreateOfferRequest {
  description: string;
  price: number;
  pickupDatetime: string;
  adminId: number;
  mobilityServiceId: number;
  pickupLocationId: number;
  returnLocationId: number;
  // version n'est pas requis pour la création
}