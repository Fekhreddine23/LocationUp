import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class LoadingService {
  private loadingSubject = new BehaviorSubject<boolean>(false);
  public loading$ = this.loadingSubject.asObservable();

  show() {
    this.loadingSubject.next(true);
  }

  hide() {
    this.loadingSubject.next(false);
  }

  // Pour les requêtes spécifiques
  private specificLoading = new Map<string, BehaviorSubject<boolean>>();

  setLoading(key: string, isLoading: boolean) {
    if (!this.specificLoading.has(key)) {
      this.specificLoading.set(key, new BehaviorSubject<boolean>(false));
    }
    this.specificLoading.get(key)!.next(isLoading);
  }

  getLoading(key: string) {
    if (!this.specificLoading.has(key)) {
      this.specificLoading.set(key, new BehaviorSubject<boolean>(false));
    }
    return this.specificLoading.get(key)!.asObservable();
  }
  
}
