import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { TwoFactorAuthService } from '../../core/services/twoFactor/two-factor-auth-service';
import { TwoFASetupResponse } from '../../core/models/twoFactor/TwoFASetupResponse.model';

@Component({
  selector: 'app-two-factor-setup',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './two-factor-setup.html',
  styleUrl: './two-factor-setup.scss'
})
export class TwoFactorSetup implements OnInit, OnChanges {

  @Input() username: string = '';
  @Output() onComplete = new EventEmitter<void>();

  isLoading = false;
  isSetupCompleted = false;
  isVerified = false;
  verificationCode = '';
  verificationError = false;
  backupCode?: string;
  qrCodeData: TwoFASetupResponse | null = null;

  constructor(private readonly twoFactorAuthService: TwoFactorAuthService) {}

  ngOnInit(): void {
    this.loadStatusIfPossible();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['username'] && !changes['username'].firstChange) {
      this.resetState();
      this.loadStatusIfPossible();
    }
  }

  startSetup(): void {
  this.isLoading = true;
  this.twoFactorAuthService.setup2FA(this.username).subscribe({
    next: (data) => {
      // Convertir l'URL otpauth en image QR Code
      if (data.qrCodeUrl.startsWith('otpauth://')) {
        data.qrCodeUrl = this.generateQrCodeImage(data.qrCodeUrl);
      }
      this.qrCodeData = data;
      this.isLoading = false;
    },
    error: (error) => {
      console.error('Erreur configuration 2FA:', error);
      this.isLoading = false;
    }
  });
}

  verifyCode(): void {
    if (!this.username || this.verificationCode.length !== 6 || this.isLoading) {
      return;
    }

    this.isLoading = true;
    this.verificationError = false;

    this.twoFactorAuthService.verify2FA(this.username, this.verificationCode).subscribe({
      next: response => {
        this.isLoading = false;
        if (response.valid) {
          this.isVerified = true;
          this.isSetupCompleted = true;
          this.fetchBackupCode();
        } else {
          this.verificationError = true;
        }
      },
      error: () => {
        this.isLoading = false;
        this.verificationError = true;
      }
    });
  }

  private loadStatusIfPossible(): void {
    if (!this.username) {
      return;
    }

    this.isLoading = true;

    this.twoFactorAuthService.get2FAStatus(this.username).subscribe({
      next: status => {
        this.isSetupCompleted = status.enabled;
        this.isVerified = status.enabled;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  private fetchBackupCode(): void {
    this.twoFactorAuthService.generateBackupCode().subscribe({
      next: response => {
        this.backupCode = response.backupCode;
      }
    });
  }

  private resetState(): void {
    this.isSetupCompleted = false;
    this.isVerified = false;
    this.verificationCode = '';
    this.verificationError = false;
    this.backupCode = undefined;
    this.qrCodeData = null;
  }



  generateQrCodeImage(otpauthUrl: string): string {
  // Utiliser un service en ligne pour générer le QR Code
  const encodedUrl = encodeURIComponent(otpauthUrl);
  return `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodedUrl}`;
}
}
