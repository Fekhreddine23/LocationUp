import { Component, OnInit, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common'; // ← AJOUTE
import { HttpClient } from '@angular/common/http'; // ← AJOUTE
import { environment } from '../environments/environment'; // ← AJOUTE

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, CommonModule], // ← AJOUTE CommonModule
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App implements OnInit {
  protected readonly title = signal('location-up-frontend');
  offers: any[] = []; // ← AJOUTE
  error: string = ''; // ← AJOUTE
  loading: boolean = true; // ← AJOUTE

  constructor(private http: HttpClient) {} // ← AJOUTE

  ngOnInit() { // ← AJOUTE
    this.loadOffers();
  }

 loadOffers() {
  this.loading = true;
  this.http.get<any[]>(`${environment.apiUrl}/offers`)
    .subscribe({
      next: (data) => {
        this.offers = data || [];
        this.loading = false;
        console.log('✅ Offres chargées:', this.offers);
      },
      error: (err) => {
        if (err.status === 0) {
          this.error = '🚨 Backend non disponible';
        } else if (err.status === 403) {
          this.error = '🔒 Accès interdit - Spring Security bloque la requête';
        } else if (err.status === 404) {
          this.error = '📭 Endpoint /api/offers non trouvé';
        } else {
          this.error = `Erreur ${err.status}: Impossible de charger les offres`;
        }
        this.loading = false;
        console.error('❌ Erreur détaillée:', err);
      }
    });
}
}