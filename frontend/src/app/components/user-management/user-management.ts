import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AdminUser } from '../../core/models/AdminUser.model';
import { UserResponse } from '../../core/models/UserResponse.model';
import { AdminService } from '../../core/services/admin.service';
import { Breadcrumbs } from '../breadcrumbs/breadcrumbs';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    Breadcrumbs
  ],
  templateUrl: './user-management.html',
  styleUrl: './user-management.scss'
})
export class UserManagement implements OnInit {

   users: AdminUser[] = [];
  filteredUsers: AdminUser[] = [];
  selectedUser: AdminUser | null = null;

  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;
  
  // Filtres
  searchQuery = '';
  statusFilter = 'ALL';
  roleFilter = 'ALL';
  
  // États
  isLoading = false;
  isEditModalOpen = false;
  isDeleteModalOpen = false;
  
  // Messages
  successMessage = '';
  errorMessage = '';

  breadcrumbItems = [
    { label: 'Administration', url: '/admin' },
    { label: 'Gestion des Utilisateurs', url: '/admin/users', active: true }
  ];

  constructor(
    private adminService: AdminService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.isLoading = true;
    this.adminService.getAllUsers(this.currentPage, this.pageSize).subscribe({
      next: (response: UserResponse) => {
        this.users = response.content;
        this.filteredUsers = [...this.users];
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Erreur chargement users:', error);
        this.errorMessage = 'Erreur lors du chargement des utilisateurs';
        this.isLoading = false;
      }
    });
  }

  searchUsers(): void {
    if (this.searchQuery.trim()) {
      this.isLoading = true;
      this.adminService.searchUsers(this.searchQuery, this.currentPage, this.pageSize).subscribe({
        next: (response: UserResponse) => {
          this.filteredUsers = response.content;
          this.totalElements = response.totalElements;
          this.totalPages = response.totalPages;
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Erreur recherche:', error);
          this.errorMessage = 'Erreur lors de la recherche';
          this.isLoading = false;
        }
      });
    } else {
      this.filteredUsers = [...this.users];
    }
  }

  applyFilters(): void {
    let filtered = [...this.users];

    // Filtre par statut
    if (this.statusFilter !== 'ALL') {
      filtered = filtered.filter(user => 
        user.status === this.statusFilter.toLowerCase()
      );
    }

    // Filtre par rôle
    if (this.roleFilter !== 'ALL') {
      filtered = filtered.filter(user => user.role === this.roleFilter);
    }

    this.filteredUsers = filtered;
  }

  // Actions sur les utilisateurs
  editUser(user: AdminUser): void {
    this.selectedUser = { ...user };
    this.isEditModalOpen = true;
  }

  updateUser(): void {
    if (!this.selectedUser) return;

    this.adminService.updateUser(this.selectedUser.id, this.selectedUser).subscribe({
      next: (updatedUser) => {
        const index = this.users.findIndex(u => u.id === updatedUser.id);
        if (index !== -1) {
          this.users[index] = updatedUser;
          this.filteredUsers = [...this.users];
        }
        this.successMessage = 'Utilisateur mis à jour avec succès';
        this.isEditModalOpen = false;
        this.clearMessagesAfterDelay();
      },
      error: (error) => {
        console.error('Erreur mise à jour:', error);
        this.errorMessage = 'Erreur lors de la mise à jour';
        this.clearMessagesAfterDelay();
      }
    });
  }

  confirmDelete(user: AdminUser): void {
    this.selectedUser = user;
    this.isDeleteModalOpen = true;
  }

  deleteUser(): void {
    if (!this.selectedUser) return;

    this.adminService.deleteUser(this.selectedUser.id).subscribe({
      next: () => {
        this.users = this.users.filter(u => u.id !== this.selectedUser!.id);
        this.filteredUsers = this.filteredUsers.filter(u => u.id !== this.selectedUser!.id);
        this.successMessage = 'Utilisateur supprimé avec succès';
        this.isDeleteModalOpen = false;
        this.clearMessagesAfterDelay();
      },
      error: (error) => {
        console.error('Erreur suppression:', error);
        this.errorMessage = 'Erreur lors de la suppression';
        this.clearMessagesAfterDelay();
      }
    });
  }

  toggleUserStatus(user: AdminUser): void {
    const action = user.status === 'active' 
      ? this.adminService.deactivateUser(user.id)
      : this.adminService.activateUser(user.id);

    action.subscribe({
      next: () => {
        user.status = user.status === 'active' ? 'inactive' : 'active';
        this.successMessage = `Utilisateur ${user.status === 'active' ? 'activé' : 'désactivé'} avec succès`;
        this.clearMessagesAfterDelay();
      },
      error: (error) => {
        console.error('Erreur changement statut:', error);
        this.errorMessage = 'Erreur lors du changement de statut';
        this.clearMessagesAfterDelay();
      }
    });
  }

  changeUserRole(user: AdminUser, newRole: string): void {
    this.adminService.changeUserRole(user.id, newRole).subscribe({
      next: (updatedUser) => {
        user.role = updatedUser.role;
        this.successMessage = 'Rôle mis à jour avec succès';
        this.clearMessagesAfterDelay();
      },
      error: (error) => {
        console.error('Erreur changement rôle:', error);
        this.errorMessage = 'Erreur lors du changement de rôle';
        this.clearMessagesAfterDelay();
      }
    });
  }

  // Pagination
  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadUsers();
    }
  }

  // Utilitaires
  getRoleText(role: string): string {
    const roles: { [key: string]: string } = {
      'ROLE_ADMIN': 'Administrateur',
      'ROLE_USER': 'Utilisateur',
      'ROLE_MODERATOR': 'Modérateur'
    };
    return roles[role] || role;
  }

  getStatusText(status: string): string {
    const statusMap: { [key: string]: string } = {
      'active': 'Actif',
      'inactive': 'Inactif',
      'suspended': 'Suspendu'
    };
    return statusMap[status] || status;
  }

  getStatusClass(status: string): string {
    return status === 'active' ? 'status-active' : 'status-inactive';
  }

  getUserInitials(user: AdminUser): string {
    if (user.firstName && user.lastName) {
      return `${user.firstName.charAt(0)}${user.lastName.charAt(0)}`.toUpperCase();
    }
    return user.username.charAt(0).toUpperCase();
  }

  clearMessagesAfterDelay(): void {
    setTimeout(() => {
      this.successMessage = '';
      this.errorMessage = '';
    }, 5000);
  }

  // Navigation
  goBackToDashboard(): void {
    this.router.navigate(['/admin']);
  }

  viewUserReservations(user: AdminUser): void {
    this.router.navigate(['/admin/reservations'], { 
      queryParams: { userId: user.id } 
    });
  }

}
