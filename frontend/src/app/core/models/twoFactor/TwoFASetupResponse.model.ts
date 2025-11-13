export interface TwoFASetupResponse {
  secret: string;
  qrCodeUrl: string;
  message: string;
}