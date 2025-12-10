import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Subscription } from 'rxjs';

import { Breadcrumbs } from '../../components/breadcrumbs/breadcrumbs';
import { SupportService, SupportRequestPayload } from '../../core/services/support.service';
import { AuthService } from '../../core/services/auth.service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-support',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, Breadcrumbs],
  templateUrl: './support.component.html',
  styleUrl: './support.component.scss'
})
export class SupportComponent implements OnInit, OnDestroy {

  supportForm: FormGroup;
  isSubmitting = false;
  submitSuccess = false;
  submitError = '';
  private routeSub?: Subscription;

  readonly breadcrumbItems = [
    { label: 'Tableau de bord', url: '/dashboard' },
    { label: 'Support & contact', url: '/support', active: true }
  ];

  constructor(
    private fb: FormBuilder,
    private supportService: SupportService,
    private authService: AuthService,
    private route: ActivatedRoute
  ) {
    const currentUser = this.authService.currentUserValue;
    this.supportForm = this.fb.group({
      subject: ['', [Validators.required, Validators.minLength(5)]],
      category: ['GENERAL', Validators.required],
      relatedId: [''],
      urgency: ['normal', Validators.required],
      message: ['', [Validators.required, Validators.minLength(20)]],
      contactEmail: [currentUser?.email || '', [Validators.email]]
    });
  }

  ngOnInit(): void {
    this.routeSub = this.route.queryParams.subscribe(params => {
      const patch: Record<string, string> = {};
      if (params['subject']) {
        patch['subject'] = params['subject'];
      }
      if (params['offerId']) {
        patch['relatedId'] = `Offre #${params['offerId']}`;
        patch['category'] = 'OFFER';
      }
      if (params['bookingId']) {
        patch['relatedId'] = `Reservation #${params['bookingId']}`;
        patch['category'] = 'BOOKING';
      }
      if (Object.keys(patch).length) {
        this.supportForm.patchValue(patch);
      }
    });
  }

  ngOnDestroy(): void {
    this.routeSub?.unsubscribe();
  }

  submit(): void {
    if (this.supportForm.invalid) {
      this.supportForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.submitError = '';
    this.submitSuccess = false;

    const payload: SupportRequestPayload = this.supportForm.value;
    this.supportService.submitRequest(payload).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.submitSuccess = true;
        this.launchEmailClient(payload);
        this.supportForm.patchValue({ message: '' });
      },
      error: () => {
        this.isSubmitting = false;
        this.submitError = 'Impossible d\'envoyer votre demande pour le moment.';
      }
    });
  }

  private launchEmailClient(payload: SupportRequestPayload): void {
    const subject = encodeURIComponent(`[${payload.category}] ${payload.subject}`);
    const bodyLines = [
      `Catégorie: ${payload.category}`,
      payload.relatedId ? `Référence: ${payload.relatedId}` : null,
      `Urgence: ${payload.urgency}`,
      payload.contactEmail ? `Contact: ${payload.contactEmail}` : null,
      '',
      payload.message,
      '',
      '---',
      'Cette demande a été générée via LocationUp.'
    ].filter(Boolean);
    const body = encodeURIComponent(bodyLines.join('\n'));
    window.open(`mailto:support@locationup.com?subject=${subject}&body=${body}`, '_blank');
  }
}
