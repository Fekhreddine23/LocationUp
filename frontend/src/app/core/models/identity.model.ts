export interface IdentityDocument {
  id?: number;
  stripeReportId?: string;
  documentType?: string;
  status?: string;
  issuingCountry?: string;
  expirationDate?: string | null;
  fileIds?: string[];
  updatedAt?: string | null;
}

export interface IdentityStatus {
  status: string;
  verified: boolean;
  reason?: string | null;
  updatedAt?: string | null;
  documents?: IdentityDocument[];
}

export interface IdentitySessionResponse {
  verificationSessionId: string;
  clientSecret: string;
  stripeStatus: string;
}
