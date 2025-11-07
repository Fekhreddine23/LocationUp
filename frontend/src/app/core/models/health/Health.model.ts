export interface HealthStatusModel {
  status: string;
  timestamp: string;
  service: string;
  database: string;
  memory?: any;
  liveness: string;
  readiness: string;
  error?: string;
}
