import { Component } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { AuthService } from './core/auth.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  showAdminLayout = false;
  showClientLayout = false;

  constructor(private router: Router, private authService: AuthService) {
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe((event: any) => {
        const url = event.urlAfterRedirects;

        // ẩn layout cho auth route
        const isAuthRoute = /^\/auth(\/.*)?$/.test(url);

        if (isAuthRoute) {
          this.showAdminLayout = false;
          this.showClientLayout = false;
          return;
        }

        // Decide layout based on current URL. Avoid forcing navigation to another route
        // so users (including admins) can still visit client pages if the app allows.
        const isAdminPath = url.startsWith('/admin');
        const isClientPath = url === '/' || (!isAdminPath && !isAuthRoute && !url.startsWith('/auth'));

        this.showAdminLayout = isAdminPath;
        this.showClientLayout = isClientPath;

        // If user is not authenticated and not on auth route, let guards handle redirects.
        // We don't perform extra navigation here to avoid redirect loops.
      });
  }
}
