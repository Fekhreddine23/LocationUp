import { AdminUser } from "./AdminUser.model";

export interface UserResponse {
  content: AdminUser[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}