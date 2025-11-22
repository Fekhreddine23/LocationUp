import { Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth-guard';
import { AdminGuard } from './core/guards/admin-guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login.component').then(m => m.LoginComponent),
    data: { breadcrumb: 'Connexion' }
  },
  {
    path: 'register',
    loadComponent: () => import('./pages/register/register.component').then(m => m.RegisterComponent),
    data: { breadcrumb: 'Inscription' }
  },
  {
    path: 'payments/success',
    loadComponent: () => import('./pages/payments/payment-success.component').then(m => m.PaymentSuccessComponent)
  },
  {
    path: 'payments/cancel',
    loadComponent: () => import('./pages/payments/payment-cancel.component').then(m => m.PaymentCancelComponent)
  },
  {
    path: 'home',
    loadComponent: () => import('./pages/home/home.component').then(m => m.HomeComponent),
    data: { breadcrumb: 'Accueil' }
  },

  // Routes protégées
  {
    path: 'dashboard',
    loadComponent: () => import('./pages/dashboard/dashboard.component').then(m => m.DashboardComponent),
    canActivate: [AuthGuard],
    data: { breadcrumb: 'Tableau de Bord' }
  },
  {
    path: 'profile',
    loadComponent: () => import('./pages/profile/profile.component').then(m => m.ProfileComponent),
    canActivate: [AuthGuard],
    data: { breadcrumb: 'Mon Profil' }
  },
  {
    path: 'offers',
    loadComponent: () => import('./pages/offers/offers.component').then(m => m.OffersComponent),
    canActivate: [AuthGuard],
    data: { breadcrumb: 'Offres' }
  },
  {
    path: 'bookings',
    loadComponent: () => import('./pages/bookings/bookings.component').then(m => m.BookingsComponent),
    canActivate: [AuthGuard],
    data: { breadcrumb: 'Mes Réservations' }
  },

  {
    path: 'bookings/new',
    loadComponent: () => import('./pages/create-booking/create-booking.component').then(m => m.CreateBookingComponent),
    canActivate: [AuthGuard],
    data: { breadcrumb: 'Nouvelle Réservation' }
  },


  {
    path: 'offers/:id',
    loadComponent: () => import('./pages/offre-detail/offre-detail').then(m => m.OffreDetail),
    data: { breadcrumb: 'Détail de l\'offre' }
  },

  {
    path: 'admin',
    loadComponent: () => import('./pages/admin/admin-dashboards/admin-dashboards').then(m => m.AdminDashboards),
    canActivate: [AdminGuard],
    data: { breadcrumb: 'Administration' }
  },
  {
    path: 'admin/users',
    loadComponent: () => import('./components/user-management/user-management').then(m => m.UserManagement),
    canActivate: [AdminGuard],
    data: { breadcrumb: 'Gestion des Utilisateurs' }
  },

  {
    path:'admin/offers',
    loadComponent: () => import('./components/offer-management/offer-management').then(m => m.OfferManagement),
    canActivate: [AdminGuard],
    data: { breadcrumb: 'Gestion des Offres' }
  },

   {
    path:'admin/bookings',
    loadComponent: () => import('./components/booking-management/booking-management').then(m => m.BookingManagement),
    canActivate: [AdminGuard],
    data: { breadcrumb: 'Gestion des Reservations' }
  },
  

  { path: '', redirectTo: '/home', pathMatch: 'full' },
  { path: '**', redirectTo: '/home' } // Route fallback
];
