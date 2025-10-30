import { Injectable } from '@angular/core';
import { BehaviorSubject, filter, map } from 'rxjs';
import { BreadcrumbItem } from '../models/BreadcrumbItem.model';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class BreadcrumbService {

  private breadcrumbsSubject = new BehaviorSubject<BreadcrumbItem[]>([]);
  public breadcrumbs$ = this.breadcrumbsSubject.asObservable();


  constructor(private router: Router, private activatedRoute: ActivatedRoute) {
    this.createBreadcrumbs();
  }

   private createBreadcrumbs(): void {
    this.router.events
      .pipe(
        filter(event => event instanceof NavigationEnd),
        map(() => this.buildBreadcrumbs(this.activatedRoute.root))
      )
      .subscribe(breadcrumbs => {
        this.breadcrumbsSubject.next(breadcrumbs);
      });
  }

  private buildBreadcrumbs(
    route: ActivatedRoute, 
    url: string = '', 
    breadcrumbs: BreadcrumbItem[] = []
  ): BreadcrumbItem[] {
    const children: ActivatedRoute[] = route.children;

    if (children.length === 0) {
      return breadcrumbs;
    }

    for (const child of children) {
      const routeURL: string = child.snapshot.url.map(segment => segment.path).join('/');
      if (routeURL !== '') {
        url += `/${routeURL}`;
      }

      const label = child.snapshot.data['breadcrumb'];
      if (label) {
        breadcrumbs.push({ label, url });
      }

      return this.buildBreadcrumbs(child, url, breadcrumbs);
    }

    return breadcrumbs;
  }

  // Méthode pour définir manuellement les breadcrumbs
  setBreadcrumbs(items: BreadcrumbItem[]): void {
    this.breadcrumbsSubject.next(items);
  }

  // Méthode pour breadcrumbs statiques
  getStaticBreadcrumbs(route: string): BreadcrumbItem[] {
    const breadcrumbsMap: { [key: string]: BreadcrumbItem[] } = {
      '/bookings': [
        { label: 'Accueil', url: '/home' },
        { label: 'Mes Réservations', url: '/bookings' }
      ],
      '/bookings/new': [
        { label: 'Accueil', url: '/home' },
        { label: 'Mes Réservations', url: '/bookings' },
        { label: 'Nouvelle Réservation', url: '/bookings/new' }
      ],
      '/offers': [
        { label: 'Accueil', url: '/home' },
        { label: 'Offres Disponibles', url: '/offers' }
      ],
      '/profile': [
        { label: 'Accueil', url: '/home' },
        { label: 'Mon Profil', url: '/profile' }
      ],
      '/dashboard': [
        { label: 'Accueil', url: '/home' },
        { label: 'Tableau de Bord', url: '/dashboard' }
      ]
    };

    return breadcrumbsMap[route] || [{ label: 'Accueil', url: '/home' }];
  }
}

  

