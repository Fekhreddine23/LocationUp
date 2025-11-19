import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { LoginRequest } from '../../core/models/auth.models';
import { TwoFactorAuthService } from '../../core/services/twoFactor/two-factor-auth-service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  // Utilise LoginRequest au lieu d'un objet générique
  credentials: LoginRequest = {
    username: '',
    password: ''  // Retirer email car LoginRequest n'a que username/password
  };
  
  isLoading = false;
  errorMessage = '';
  lockoutUntil = 0;
  remainingLockDuration = '';
  twoFactorRequired = false;
  twoFactorCode = '';
  twoFactorError = '';
  twoFactorMessage = '';
  isVerifyingTwoFactor = false;
  private static readonly LOCKOUT_ENABLED = false;
  private static readonly MAX_FAILED_ATTEMPTS = 5;
  private static readonly LOCKOUT_DURATION_MS = 5 * 60 * 1000;
  private static readonly ATTEMPT_STORAGE_KEY = 'auth_failed_attempts';
  private static readonly LOCKOUT_STORAGE_KEY = 'auth_lockout_until';

  constructor(
    private authService: AuthService,
    private router: Router,
    private twoFactorService: TwoFactorAuthService
  ) {
    if (LoginComponent.LOCKOUT_ENABLED) {
      this.restoreSecurityState();
    }
  }

  get isLockedOut(): boolean {
    return this.lockoutUntil > Date.now();
  }

  onLogin(): void {
    if (LoginComponent.LOCKOUT_ENABLED && this.isLockedOut) {
      this.updateRemainingLockDuration();
      this.errorMessage = `Compte temporairement bloqué. Réessayez dans ${this.remainingLockDuration}.`;
      return;
    }

    if (this.twoFactorRequired) {
      this.verifyTwoFactorCode();
      return;
    }

    // Validation basique
    if (!this.credentials.username || !this.credentials.password) {
      this.errorMessage = 'Veuillez remplir tous les champs';
      return;
    }

    this.sanitizeCredentials();
    this.isLoading = true;
    this.errorMessage = '';

    this.twoFactorService.get2FAStatus(this.credentials.username).subscribe({
      next: (status) => {
        if (status.enabled) {
          this.isLoading = false;
          this.twoFactorRequired = true;
          this.twoFactorMessage = 'Entrez le code généré par votre application d’authentification.';
        } else {
          this.performLogin();
        }
      },
      error: () => {
        this.performLogin();
      }
    });
  }

  private verifyTwoFactorCode(): void {
    if (!this.twoFactorCode || this.twoFactorCode.trim().length !== 6) {
      this.twoFactorError = 'Code invalide (6 chiffres requis).';
      return;
    }

    this.twoFactorError = '';
    this.isVerifyingTwoFactor = true;
    const code = this.twoFactorCode.trim();

    this.twoFactorService.verify2FA(this.credentials.username, code).subscribe({
      next: (response) => {
        this.isVerifyingTwoFactor = false;
        if (response.valid) {
          this.twoFactorRequired = false;
          this.twoFactorCode = '';
          this.twoFactorMessage = '';
          this.performLogin();
        } else {
          this.twoFactorError = response.message || 'Code incorrect';
          this.registerFailedAttempt();
        }
      },
      error: () => {
        this.isVerifyingTwoFactor = false;
        this.twoFactorError = 'Impossible de vérifier le code. Réessayez.';
        this.registerFailedAttempt();
      }
    });
  }

  private performLogin(): void {
    this.isLoading = true;

    this.authService.login(this.credentials).subscribe({
      next: () => {
        this.isLoading = false;
        this.resetSecurityState();
        this.router.navigate(['/dashboard']);
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = error.error?.message || 'Erreur de connexion. Veuillez réessayer.';
        console.error('Login error:', error);
        this.registerFailedAttempt();
      }
    });
  }

  private sanitizeCredentials(): void {
    this.credentials.username = this.credentials.username.trim();
    this.credentials.password = this.credentials.password.trim();
  }

  cancelTwoFactorStep(): void {
    this.twoFactorRequired = false;
    this.twoFactorCode = '';
    this.twoFactorMessage = '';
    this.twoFactorError = '';
  }

  private registerFailedAttempt(): void {
    if (!LoginComponent.LOCKOUT_ENABLED) {
      return;
    }
    const attempts = Number(localStorage.getItem(LoginComponent.ATTEMPT_STORAGE_KEY) || '0') + 1;
    localStorage.setItem(LoginComponent.ATTEMPT_STORAGE_KEY, attempts.toString());

    if (attempts >= LoginComponent.MAX_FAILED_ATTEMPTS) {
      this.lockoutUntil = Date.now() + LoginComponent.LOCKOUT_DURATION_MS;
      localStorage.setItem(LoginComponent.LOCKOUT_STORAGE_KEY, this.lockoutUntil.toString());
      this.updateRemainingLockDuration();
      this.errorMessage = `Trop de tentatives. Compte bloqué pendant ${this.remainingLockDuration}.`;
    }
  }

  private restoreSecurityState(): void {
    const storedLockout = localStorage.getItem(LoginComponent.LOCKOUT_STORAGE_KEY);
    this.lockoutUntil = storedLockout ? Number(storedLockout) : 0;
    if (this.isLockedOut) {
      this.updateRemainingLockDuration();
    } else {
      this.clearLockout();
    }
  }

  private updateRemainingLockDuration(): void {
    const remaining = Math.max(0, this.lockoutUntil - Date.now());
    const minutes = Math.floor(remaining / 60000);
    const seconds = Math.floor((remaining % 60000) / 1000);
    this.remainingLockDuration = `${minutes}m ${seconds}s`;
  }

  private resetSecurityState(): void {
    localStorage.removeItem(LoginComponent.ATTEMPT_STORAGE_KEY);
    this.clearLockout();
  }

  private clearLockout(): void {
    this.lockoutUntil = 0;
    this.remainingLockDuration = '';
    localStorage.removeItem(LoginComponent.LOCKOUT_STORAGE_KEY);
  }
}
