export interface MonthlyReservationStat {
  month: string;
  reservations: number;
  revenue: number;
}

export interface CategoryStat {
  category: string;
  count: number;
}

export interface CityStat {
  city: string;
  count: number;
}

export interface DashboardTrends {
  reservationsByMonth: MonthlyReservationStat[];
  offersByCategory: CategoryStat[];
  topPickupCities: CityStat[];
}
