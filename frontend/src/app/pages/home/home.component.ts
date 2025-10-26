import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OffersService } from '../../core/services/offers.service';


@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent {
  loading = false;
  error = '';
  offers: any[] = [];

  constructor(private offersService: OffersService) { }

  ngOnInit(): void {
    this.loadOffers();
  }

  loadOffers(): void {
    this.loading = true;
    this.error = '';

    this.offersService.getAllOffers().subscribe({
      next: (offers) => {
        this.offers = offers;
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Erreur lors du chargement des offres';
        this.loading = false;
        console.error('Error loading offers:', error);
      }
    });
  }
}