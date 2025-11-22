export type PaymentStatus = 'PENDING' | 'REQUIRES_ACTION' | 'PAID' | 'FAILED' | 'REFUNDED';

export interface AdminBooking {
  reservationId: number;
  reservationDate: string;
  status: 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED';
  createdAt?: string;
  totalPrice?: number;
  paymentStatus?: PaymentStatus;
  paymentReference?: string;
  paymentDate?: string;
  user?: {
    id: number;
    username: string;
    email?: string;
  };
  offer?: {
    offerId: number;
    mobilityService?: string;
    pickupLocation?: string;
    price?: number;
  };
}



export interface BookingResponse {
  content: AdminBooking[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
