export interface PaymentStatusBreakdown {
  status: string;
  count: number;
  amount: number;
}

export interface MonthlyRevenuePoint {
  label: string;
  revenue: number;
  payments: number;
}

export interface PaymentAlert {
  reservationId: number;
  customer: string;
  amount: number;
  paymentStatus: string;
  severity: string;
  message: string;
  reservationDate: string;
}

export interface PaymentEventLogEntry {
  id: number;
  eventId: string;
  reservationReference: string;
  type: string;
  status: string;
  errorMessage?: string;
  receivedAt: string;
}

export interface OutstandingPoint {
  period: string;
  count: number;
  amount: number;
}

export interface FinanceOverview {
  totalRevenue: number;
  monthToDateRevenue: number;
  outstandingRevenue: number;
  confirmationRate: number;
  paymentsByStatus: PaymentStatusBreakdown[];
  revenueHistory: MonthlyRevenuePoint[];
  alerts: PaymentAlert[];
  outstandingByWeek: OutstandingPoint[];
  outstandingByMonth: OutstandingPoint[];
  identitiesTotal: number;
  identitiesVerified: number;
  identitiesProcessing: number;
  identitiesRequiresInput: number;
  identitiesPending: number;
}

export interface FinanceAlertFilters {
  severity?: 'ALERTE' | 'CRITIQUE';
  statuses?: string[];
  search?: string;
  startDate?: string;
  endDate?: string;
  actionRequiredOnly?: boolean;
  limit?: number;
}
