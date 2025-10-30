import { User } from "./auth.models";

export interface AdminUser extends User {
  createdAt: string;
  bookingCount: number;
  firstName?: string;
  lastName?: string;
  lastLogin?: string;
  email?: string;
  status?: 'active' | 'inactive' | 'suspended';
 
  
}
