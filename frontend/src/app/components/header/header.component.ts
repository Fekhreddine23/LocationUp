import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { User } from '../../core/models/auth.models';
import { NotificationComponent } from '../notification/notification';
import { Theme, ThemeService } from '../../core/services/theme/theme-service';
import { Observable } from 'rxjs';
import { HasRoleDirective } from '../../core/directives/has-role.directive';


@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink, NotificationComponent, HasRoleDirective],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent {
 
  isLoggedIn = false;
  currentUser: User | null = null;
  isMenuOpen = false;
  isMobileMenuOpen = false;
  adminLinksOpen = false;
  authResolved = false;
  readonly skeletonPills = Array.from({ length: 4 });
  adminNavLinks = [
    { label: 'Tableau de bord', route: '/admin', icon: '📊' },
    { label: 'Finances', route: '/admin/finance', icon: '💶' },
    { label: 'Utilisateurs', route: '/admin/users', icon: '👥' },
    { label: 'Réservations', route: '/admin/bookings', icon: '📅' },
    { label: 'Offres', route: '/admin/offers', icon: '📦' }
  ];

  // gestion du theme 
  currentTheme$: Observable<Theme>; 
  isDarkMode = false; 

  constructor(
    private authService: AuthService,
    private router: Router, 
    private themeService: ThemeService 
  ) {
    this.currentTheme$ = this.themeService.currentTheme$;

  }

  ngOnInit() {
    this.authService.currentUser.subscribe(user => {
      this.isLoggedIn = !!user;
      this.currentUser = user;
      this.authResolved = true;
    });
     //souscrire aux changements de thème
     this.currentTheme$.subscribe(theme => {
      this.isDarkMode = theme === 'dark';
    });
  }

  toggleMenu(): void {
    this.isMenuOpen = !this.isMenuOpen;
  }

  toggleMobileMenu(): void {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }

  closeMobileMenu(): void {
    this.isMobileMenuOpen = false;
    this.adminLinksOpen = false;
  }

  logout(): void {
    this.authService.logout();
    this.isMenuOpen = false;
    this.isMobileMenuOpen = false;
    this.router.navigate(['/']);
  }

   //AJOUT: Méthode pour basculer le thème
  toggleTheme(): void {
    this.themeService.toggleTheme();
  }

  toggleAdminLinks(): void {
    this.adminLinksOpen = !this.adminLinksOpen;
  }

  get isAdmin(): boolean {
    return this.authService.hasRole('ROLE_ADMIN');
  }

  get dashboardRoute(): string {
    return this.isAdmin ? '/admin' : '/dashboard';
  }

  get offersRoute(): string {
    return this.isAdmin ? '/admin/offers' : '/offers';
  }

  get bookingsRoute(): string {
    return this.isAdmin ? '/admin/bookings' : '/bookings';
  }

  get favoritesRoute(): string {
    return '/favorites';
  }

  navigateTo(route: string): void {
    this.router.navigate([route]);
    this.closeMobileMenu();
  }
}
