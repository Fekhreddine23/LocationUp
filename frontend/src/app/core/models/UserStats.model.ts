export interface UserStats {
  totalBookings: number;
  activeBookings: number;
  cancelledBookings: number;
  completedBookings: number;
  favoriteVehicleType?: string;
  totalSpent?: number;
  memberSince: string;
}