export interface PaymentEvent {
  id: number;
  eventId: string;
  reservationReference: string;
  type: string;
  status: string;
  errorMessage?: string;
  receivedAt: string;
}
