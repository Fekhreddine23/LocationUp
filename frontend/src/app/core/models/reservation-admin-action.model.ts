export interface ReservationAdminAction {
  id: number;
  reservationId: number;
  adminUsername: string;
  actionType: string;
  details?: string;
  createdAt: string;
}
