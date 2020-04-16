import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { of, from, Observable } from 'rxjs';
import { AngularFireAuth } from '@angular/fire/auth';
import { map, switchMap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class UnauthorizedGuard implements CanActivate {

  constructor(private afa: AngularFireAuth, private router: Router) { }

  canActivate(next: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
    return this.afa.user.pipe(switchMap(user => {
      if (!user) {
        this.router.navigateByUrl('login');
        return of(false);
      }
      return from(user.getIdTokenResult()).pipe(map((tokenData) => {
        if (tokenData.claims.role === 'dispatcher') {
          this.router.navigateByUrl('dashboard');
          return false;
        } else {
          return true;
        }
      }));
    }));
  }
}
