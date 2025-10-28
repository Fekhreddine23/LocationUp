import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}
canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
  const currentUser = this.authService.currentUserValue;
  console.log('🔐 [AuthGuard] Checking access to:', state.url);
  console.log('🔐 [AuthGuard] Current user:', currentUser);
  console.log('🔐 [AuthGuard] Is authenticated:', !!currentUser);
  
  if (currentUser) {
    return true;
  }
  
  console.log('🔐 [AuthGuard] Redirecting to login');
  this.router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
  return false;
}
}