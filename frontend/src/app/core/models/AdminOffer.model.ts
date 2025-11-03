import { Offer, OfferStatus } from './offer.model';

export type AdminOffer = Offer & {
  active?: boolean;
};

export interface AdminOfferForm {
  pickupLocation: string;
  pickupLocationCity: string;
  returnLocation: string;
  returnLocationCity: string;
  mobilityService: string;
  pickupDatetime: string;
  description?: string;
  price: number;
  status?: OfferStatus;
  adminId: number | null;
}
