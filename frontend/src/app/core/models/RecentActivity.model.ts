export interface RecentActivity {
    id: number;
  type: 'RESERVATION' | 'USER' | 'OFFER';
  description: string;
  timestamp: string;
  user?: {
    id: number;
    username: string;
    email: string;
  };
}
