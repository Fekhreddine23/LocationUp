import { CommonModule, JsonPipe } from '@angular/common';
import { Component } from '@angular/core';
import { AdminService } from '../../core/services/admin.service';

@Component({
  selector: 'app-integration-test',
  standalone: true,
  imports: [CommonModule, JsonPipe],
  templateUrl: './integration-test.html',
  styleUrl: './integration-test.scss'
})
export class IntegrationTest {

   testing = false;
  loading = false;
  testResult: any = null;

  constructor(private adminService: AdminService) {}

  async testStats() {
    await this.testEndpoint('stats', () => this.adminService.getAdminStats());
  }

  async testUsers() {
    await this.testEndpoint('users', () => this.adminService.getAllUsers());
  }

  async testOffers() {
    await this.testEndpoint('offers', () => this.adminService.getAllOffers());
  }

  async testBookings() {
    await this.testEndpoint('bookings', () => this.adminService.getAllBookings());
  }

  private async testEndpoint(name: string, apiCall: () => any) {
    this.testing = true;
    this.loading = true;
    this.testResult = null;

    try {
      const data = await apiCall().toPromise();
      this.testResult = {
        success: true,
        data: data,
        message: `Endpoint ${name} fonctionne correctement`
      };
      console.log(`✅ ${name}:`, data);
    } catch (error: any) {
      this.testResult = {
        success: false,
        error: error.message,
        data: null,
        message: `Erreur avec l'endpoint ${name}`
      };
      console.error(`❌ ${name}:`, error);
    } finally {
      this.testing = false;
      this.loading = false;
    }
  }

}
