import { Injectable } from "@angular/core";
import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
  HttpErrorResponse
} from "@angular/common/http";
import { Observable, throwError, BehaviorSubject } from "rxjs";
import { catchError, filter, take, switchMap } from "rxjs/operators";
import { Router } from "@angular/router";
import { AuthService } from "../auth.service";

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  private isRefreshing = false;
  private refreshTokenSubject: BehaviorSubject<string | null> = new BehaviorSubject<string | null>(null);

  constructor(private authService: AuthService, private router: Router) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.authService.getToken();
    let request = req;
    if (token) {
      request = this.addTokenHeader(req, token);
    }

    return next.handle(request).pipe(
      catchError(err => {
        if (err instanceof HttpErrorResponse && err.status === 401) {
          // Nếu đang refresh token, chờ token mới
          if (!this.isRefreshing) {
            this.isRefreshing = true;
            this.refreshTokenSubject.next(null);

            return this.authService.refreshToken().pipe(
              switchMap((res: any) => {
                this.isRefreshing = false;

                const newAccessToken = res.data.accessToken;
                const newRefreshToken = res.data.refreshToken;

                this.authService.saveToken(newAccessToken);
                if (newRefreshToken) this.authService.saveRefreshToken(newRefreshToken);

                this.refreshTokenSubject.next(newAccessToken);

                return next.handle(this.addTokenHeader(req, newAccessToken));
              }),
              catchError(err2 => {
                // Refresh token hết hạn hoặc lỗi → logout
                this.isRefreshing = false;
                this.authService.logout();
                this.router.navigate(['/auth/login']);
                return throwError(() => err2);
              })
            );
          } else {
            // Chờ refresh token hoàn thành
            return this.refreshTokenSubject.pipe(
              filter(token => token != null),
              take(1),
              switchMap(token => next.handle(this.addTokenHeader(req, token!)))
            );
          }
        }

        return throwError(() => err);
      })
    );
  }

  private addTokenHeader(request: HttpRequest<any>, token: string) {
    return request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }
}
