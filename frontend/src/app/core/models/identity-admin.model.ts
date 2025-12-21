export interface IdentityVerificationRecord {
  id?: number;
  userId?: number;
  reservationId?: number;
  status?: string;
  reason?: string;
  stripeSessionId?: string;
  documentType?: string;
  createdAt?: string;
  updatedAt?: string;
}
