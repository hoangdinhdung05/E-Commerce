import { Injectable } from '@angular/core';
import { CanMatch, CanActivate, Route, UrlSegment, Router, UrlTree, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthService } from '../auth.service';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanMatch, CanActivate {
  constructor(private auth: AuthService, private router: Router) {}

  canMatch(_route: Route, _segments: UrlSegment[]): boolean | UrlTree {
    return this.auth.isAuthenticated()
      ? true
      : this.router.createUrlTree(['/auth/login']);
  }

  canActivate(_route: ActivatedRouteSnapshot, _state: RouterStateSnapshot): boolean | UrlTree {
    return this.auth.isAuthenticated()
      ? true
      : this.router.createUrlTree(['/auth/login']);
  }
}
