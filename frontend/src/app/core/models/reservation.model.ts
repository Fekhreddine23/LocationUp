export interface ReservationDTO {
  reservationId?: number;
  userId: number;
  offerId: number;
  reservationDate: string; // LocalDateTime devient string pour JSON
  status: string;
  createdAt?: string;
  updatedAt?: string;
}

// Pour la création de réservation
export interface CreateReservationRequest {
  userId: number;
  offerId: number;
  reservationDate: string;
}

// Pour la mise à jour
export interface UpdateReservationRequest {
  userId?: number;
  offerId?: number;
  reservationDate?: string;
  status?: string;
}