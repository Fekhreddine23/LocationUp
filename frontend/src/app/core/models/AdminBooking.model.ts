import { IdentityDocument } from './identity.model';
import { DriverProfile } from './driver-profile.model';

export type PaymentStatus = 'PENDING' | 'REQUIRES_ACTION' | 'PAID' | 'FAILED' | 'REFUNDED' | 'EXPIRED';

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
  identityStatus?: string;
  identityUpdatedAt?: string;
  identityReason?: string;
  identityDocuments?: IdentityDocument[];
  driverProfile?: DriverProfile;
}



export interface BookingResponse {
  content: AdminBooking[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface BookingExportFilters {
  query?: string;
  status?: string;
  startDate?: string;
  endDate?: string;
  anomaliesOnly?: boolean;
  userId?: number;
}
