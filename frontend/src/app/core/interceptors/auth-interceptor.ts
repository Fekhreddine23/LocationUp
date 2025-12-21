// auth.interceptor.ts
import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable, throwError, switchMap, catchError } from 'rxjs';
import { AuthService } from '../services/auth.service';


@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  
  constructor(private authService: AuthService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (req.headers.has('X-Skip-Auth')) {
      const cleaned = req.clone({
        headers: req.headers.delete('X-Skip-Auth')
      });
      return next.handle(cleaned);
    }

    // Récupère le token JWT
    const authToken = this.authService.getToken();

    if (authToken) {
      // Clone la requête et ajoute le header Authorization
      const authReq = req.clone({
        headers: req.headers.set('Authorization', `Bearer ${authToken}`)
      });
      return next.handle(authReq).pipe(
        catchError(err => {
          if (err.status === 401) {
            return this.authService.refreshAccessToken().pipe(
              switchMap(res => {
                const newToken = res?.token;
                if (!newToken) {
                  return throwError(() => err);
                }
                const retryReq = req.clone({
                  headers: req.headers.set('Authorization', `Bearer ${newToken}`)
                });
                return next.handle(retryReq);
              }),
              catchError(() => throwError(() => err))
            );
          }
          return throwError(() => err);
        })
      );
    }

    return next.handle(req).pipe(
      catchError(err => {
        if (err.status === 401) {
          return this.authService.refreshAccessToken().pipe(
            switchMap(res => {
              const newToken = res?.token;
              if (!newToken) {
                return throwError(() => err);
              }
              const retryReq = req.clone({
                headers: req.headers.set('Authorization', `Bearer ${newToken}`)
              });
              return next.handle(retryReq);
            }),
            catchError(() => throwError(() => err))
          );
        }
        return throwError(() => err);
      })
    );
  }
}
