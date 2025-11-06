import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { User } from '../../core/models/auth.models';
import { NotificationComponent } from '../notification/notification';


@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink, NotificationComponent],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent {
 
  isLoggedIn = false;
  currentUser: User | null = null;
  isMenuOpen = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.authService.currentUser.subscribe(user => {
      this.isLoggedIn = !!user;
      this.currentUser = user;
    });
  }

  toggleMenu(): void {
    this.isMenuOpen = !this.isMenuOpen;
  }

  logout(): void {
    this.authService.logout();
    this.isMenuOpen = false;
    this.router.navigate(['/']);
  }

  get isAdmin(): boolean {
    return this.currentUser?.role === 'ROLE_ADMIN';
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
}
