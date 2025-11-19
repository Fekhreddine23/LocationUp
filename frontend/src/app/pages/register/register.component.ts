import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent {
  userData = {
    username: '',
    email: '',
    password: '',
    confirmPassword: ''
  };
  
  isLoading = false;
  errorMessage = '';
  passwordsMatch = true;
  passwordStrengthLabel: 'faible' | 'moyen' | 'fort' | 'excellent' = 'faible';
  passwordCriteria = [
    { label: '12 caractères minimum', met: false },
    { label: 'Au moins une majuscule', met: false },
    { label: 'Au moins une minuscule', met: false },
    { label: 'Au moins un chiffre', met: false },
    { label: 'Au moins un caractère spécial', met: false }
  ];
  hasConsentedToSecurityPolicy = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  checkPasswords(): void {
    this.passwordsMatch = this.userData.password === this.userData.confirmPassword;
    this.evaluatePasswordSecurity();
  }

  private evaluatePasswordSecurity(): void {
    const value = this.userData.password || '';
    const normalized = value.trim();

    this.passwordCriteria = [
      { label: '12 caractères minimum', met: normalized.length >= 12 },
      { label: 'Au moins une majuscule', met: /[A-Z]/.test(normalized) },
      { label: 'Au moins une minuscule', met: /[a-z]/.test(normalized) },
      { label: 'Au moins un chiffre', met: /\d/.test(normalized) },
      { label: 'Au moins un caractère spécial', met: /[^A-Za-z0-9]/.test(normalized) }
    ];

    const fulfilled = this.passwordCriteria.filter(rule => rule.met).length;
    if (fulfilled <= 2) {
      this.passwordStrengthLabel = 'faible';
    } else if (fulfilled === 3) {
      this.passwordStrengthLabel = 'moyen';
    } else if (fulfilled === 4) {
      this.passwordStrengthLabel = 'fort';
    } else {
      this.passwordStrengthLabel = 'excellent';
    }
  }

  get isPasswordCompliant(): boolean {
    return this.passwordCriteria.every(rule => rule.met);
  }

  onRegister(): void {
    this.checkPasswords();
    
    if (!this.passwordsMatch) {
      this.errorMessage = 'Les mots de passe ne correspondent pas';
      return;
    }

    if (!this.isPasswordCompliant) {
      this.errorMessage = 'Votre mot de passe doit respecter toutes les règles de sécurité.';
      return;
    }

    if (!this.hasConsentedToSecurityPolicy) {
      this.errorMessage = 'Vous devez accepter la politique de sécurité.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    const { confirmPassword, ...registerData } = this.userData;
    registerData.username = registerData.username.trim();
    registerData.email = registerData.email.trim().toLowerCase();

    this.authService.register(registerData).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.router.navigate(['/dashboard']);
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = error.error?.message || 'Erreur lors de l\'inscription';
        console.error('Register error:', error);
      }
    });
  }
}
