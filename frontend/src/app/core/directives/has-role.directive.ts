import { Directive, Input, OnDestroy, TemplateRef, ViewContainerRef } from '@angular/core';
import { Subscription } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { User } from '../models/auth.models';

@Directive({
  selector: '[appHasRole]',
  standalone: true
})
export class HasRoleDirective implements OnDestroy {
  private allowedRoles: string[] = [];
  private user: User | null = null;
  private subscription: Subscription;

  constructor(
    private templateRef: TemplateRef<unknown>,
    private viewContainerRef: ViewContainerRef,
    private authService: AuthService
  ) {
    this.subscription = this.authService.currentUser.subscribe(user => {
      this.user = user;
      this.updateView();
    });
  }

  @Input()
  set appHasRole(role: string | string[]) {
    this.allowedRoles = Array.isArray(role) ? role : [role];
    this.updateView();
  }

  private updateView(): void {
    if (!this.allowedRoles || this.allowedRoles.length === 0) {
      this.viewContainerRef.clear();
      return;
    }

    const canDisplay = !!this.user?.role && this.allowedRoles.includes(this.user.role);
    this.viewContainerRef.clear();

    if (canDisplay) {
      this.viewContainerRef.createEmbeddedView(this.templateRef);
    }
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }
}
