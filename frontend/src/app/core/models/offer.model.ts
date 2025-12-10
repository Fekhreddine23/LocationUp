export type OfferStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED';

export interface Offer {
  offerId: number;
  description: string;
  price: number;
  pickupDatetime: string;
  createdAt: string;
  updatedAt: string;
  version: number; // ← Doit être présent et obligatoire
  adminId?: number;
  mobilityServiceId?: number;
  pickupLocationId?: number;
  returnLocationId?: number;
  pickupLocationName?: string;
  returnLocationName?: string;
  pickupLocationCity?: string;
  returnLocationCity?: string;
  status?: OfferStatus;
  mobilityService?: string;
  mobilityServiceName?: string;
  pickupLocation?: string;
  returnLocation?: string;
  adminName?: string;
  imageUrl?: string;
  pickupLatitude?: number;
  pickupLongitude?: number;
  returnLatitude?: number;
  returnLongitude?: number;
  favorite?: boolean;
}

export interface CreateOfferRequest {
  // CHAMPS OBLIGATOIRES pour le backend
  pickupLocationName: string;        // ← OBLIGATOIRE - anciennement pickupLocation
  returnLocationName: string;        // ← OBLIGATOIRE - anciennement returnLocation
  mobilityServiceId: number;         // ← OBLIGATOIRE - ID numérique, pas le nom
  
  // Champs existants
  pickupDatetime: string;
  price: number;
  description: string;
  
  // Champs optionnels
  adminId?: number;
  status?: OfferStatus;
  active?: boolean;
  
  // SUPPRIMEZ ces champs - ils ne sont pas dans le DTO backend
  // mobilityService?: string;        // ❌ SUPPRIMER
  // pickupLocation?: string;         // ❌ SUPPRIMER  
  // returnLocation?: string;         // ❌ SUPPRIMER
  // pickupLocationCity?: string;     // ❌ SUPPRIMER
  // returnLocationCity?: string;     // ❌ SUPPRIMER
}
