import { Injectable } from '@angular/core';
import { environment } from '../../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TwoFASetupResponse } from '../../models/twoFactor/TwoFASetupResponse.model';
import { TwoFAVerifyResponse } from '../../models/twoFactor/TwoFAVerifyResponse.model';
import { TwoFAStatusResponse } from '../../models/twoFactor/TwoFAStatusResponse.model';

@Injectable({
  providedIn: 'root'
})
export class TwoFactorAuthService {

  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}


   /**
   * Configure le 2FA pour un utilisateur
   */
  setup2FA(username: string): Observable<TwoFASetupResponse> {
    return this.http.post<TwoFASetupResponse>(`${this.baseUrl}/api/2fa/setup`, null, {
      params: { username }
    });
  }


  /**
   * Vérifie un code 2FA
   */
  verify2FA(username: string, code: string): Observable<TwoFAVerifyResponse> {
    return this.http.post<TwoFAVerifyResponse>(`${this.baseUrl}/api/2fa/verify`, null, {
      params: { username, code }
    });
  }


  /**
   * Récupère le statut 2FA
   */
  get2FAStatus(username: string): Observable<TwoFAStatusResponse> {
    return this.http.get<TwoFAStatusResponse>(`${this.baseUrl}/api/2fa/status`, {
      params: { username }
    });
  }


   /**
   * Désactive le 2FA
   */
  disable2FA(username: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/2fa/disable`, null, {
      params: { username }
    });
  }


  /**
   * Génère un code de secours
   */
  generateBackupCode(): Observable<{ backupCode: string; message: string }> {
    return this.http.get<{ backupCode: string; message: string }>(`${this.baseUrl}/api/2fa/backup-code`);
  }
  
}
