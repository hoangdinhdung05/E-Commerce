import { Injectable } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot, UrlTree } from '@angular/router';
import { AuthService } from '../auth.service';
import { ToastrService } from 'ngx-toastr';

@Injectable({ providedIn: 'root' })
export class RoleGuard implements CanActivate {

  constructor(
    private authService: AuthService,
    private router: Router,
    private toastr: ToastrService
  ) {}

  canActivate(route: ActivatedRouteSnapshot): boolean | UrlTree {
    const expected = route.data['role'];
    const expectedRoles: string[] = Array.isArray(expected) ? expected : (typeof expected === 'string' ? [expected] : []);
    const userRoles = this.authService.getRoles();

    // Not logged in
    if (!userRoles || !userRoles.length) {
      return this.router.createUrlTree(['/auth/login']);
    }

    // If any of user's roles matches expected roles, allow
    const allowed = userRoles.some(r => expectedRoles.includes(r));
    if (!allowed) {
      this.toastr.warning('Bạn không có quyền truy cập vào trang này!');
        return this.router.createUrlTree(['/forbidden']);
    }

    return true;
  }
}
