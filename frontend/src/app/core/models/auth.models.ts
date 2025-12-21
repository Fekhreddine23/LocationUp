export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  username: string;
  role: string;
  userId: number;
  avatarUrl?: string;
}

export interface User {
  id: number;
  username: string;
  role: string;
  email?: string;
  token?: string;
  createdAt?: string;
  bookingCount?: number;
  avatarUrl?: string;
}
