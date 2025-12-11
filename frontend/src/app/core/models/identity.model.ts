export interface IdentityStatus {
  status: string;
  verified: boolean;
  reason?: string | null;
  updatedAt?: string | null;
}

export interface IdentitySessionResponse {
  verificationSessionId: string;
  clientSecret: string;
  stripeStatus: string;
}
