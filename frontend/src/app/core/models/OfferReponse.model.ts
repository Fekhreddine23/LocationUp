import { Offer } from "./offer.model";

export interface OfferResponse {
  content: Offer[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}