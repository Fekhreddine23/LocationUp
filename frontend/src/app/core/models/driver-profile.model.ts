export interface DriverProfile {
  licenseNumber?: string;
  licenseCountry?: string;
  licenseCategory?: string;
  licenseIssuedOn?: string;
  licenseExpiresOn?: string;
  annualKilometers?: number;
  usageReason?: string;
  notes?: string;
  updatedAt?: string;
}

export const createEmptyDriverProfile = (): DriverProfile => ({
  licenseNumber: '',
  licenseCountry: '',
  licenseCategory: '',
  licenseIssuedOn: '',
  licenseExpiresOn: '',
  annualKilometers: undefined,
  usageReason: '',
  notes: ''
});
