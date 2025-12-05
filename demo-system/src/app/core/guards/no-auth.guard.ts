import { Injectable } from '@angular/core';
import { CanActivate, Router, UrlTree, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthService } from '../auth.service';

@Injectable({ providedIn: 'root' })
export class NoAuthGuard implements CanActivate {
  constructor(private auth: AuthService, private router: Router) {}

  canActivate(_route: ActivatedRouteSnapshot, _state: RouterStateSnapshot): boolean | UrlTree {
    // Nếu đã đăng nhập, chuyển hướng về trang phù hợp dựa trên role
    if (this.auth.isAuthenticated()) {
      const role = this.auth.getUserRole();
      
      // Admin -> admin dashboard
      if (role === 'ROLE_ADMIN') {
        return this.router.createUrlTree(['/admin']);
      }
      
      // User hoặc các role khác -> client home
      return this.router.createUrlTree(['/']);
    }
    
    // Chưa đăng nhập -> cho phép truy cập
    return true;
  }
}
