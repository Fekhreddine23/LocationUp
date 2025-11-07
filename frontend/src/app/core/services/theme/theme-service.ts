import { isPlatformBrowser } from '@angular/common';
import { Inject, Injectable, PLATFORM_ID } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';



export type Theme = 'light' | 'dark';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {


  private currentThemeSubject: BehaviorSubject<Theme>;
  public currentTheme$: Observable<Theme>;


  constructor(@Inject(PLATFORM_ID) private platformId: any) {
    // Récupérer le thème sauvegardé ou utiliser 'light' par défaut
    const savedTheme = this.getSavedTheme();
    this.currentThemeSubject = new BehaviorSubject<Theme>(savedTheme);
    this.currentTheme$ = this.currentThemeSubject.asObservable();
    
    // Appliquer le thème au démarrage
    this.applyTheme(savedTheme);
  }


   toggleTheme(): void {
    const newTheme = this.currentThemeSubject.value === 'light' ? 'dark' : 'light';
    this.setTheme(newTheme);
  }

  setTheme(theme: Theme): void {
    this.currentThemeSubject.next(theme);
    this.applyTheme(theme);
    this.saveTheme(theme);
  }

  getCurrentTheme(): Theme {
    return this.currentThemeSubject.value;
  }

  private applyTheme(theme: Theme): void {
    if (isPlatformBrowser(this.platformId)) {
      const htmlElement = document.documentElement;
      
      if (theme === 'dark') {
        htmlElement.classList.add('dark-theme');
        htmlElement.classList.remove('light-theme');
      } else {
        htmlElement.classList.add('light-theme');
        htmlElement.classList.remove('dark-theme');
      }
    }
  }

  private getSavedTheme(): Theme {
    if (isPlatformBrowser(this.platformId)) {
      const saved = localStorage.getItem('theme');
      // Vérifier aussi la préférence système
      const systemPrefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
      
      if (saved === 'light' || saved === 'dark') {
        return saved as Theme;
      }
      
      return systemPrefersDark ? 'dark' : 'light';
    }
    return 'light';
  }

  private saveTheme(theme: Theme): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem('theme', theme);
    }
  }
  
}
