export interface AdminBooking {
  reservationId: number;
  reservationDate: string;
  status: 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED';
  createdAt?: string;
  totalPrice?: number;
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