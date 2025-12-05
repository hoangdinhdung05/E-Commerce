import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError, of } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from 'src/environments/environment';
import { LoginRequest } from './models/request/login-request';
import { RegisterRequest } from './models/request/register-request';
import { BaseResponse } from './models/response/base-response';
import { AuthResponse } from './models/response/auth-response';
import { jwtDecode } from 'jwt-decode';

interface JwtPayload {
  exp?: number;                 // seconds epoch
  roles?: string | string[];    // "ROLE_USER" | ["ROLE_USER", ...]
  role?: string;                // fallback
  sub?: string;
  userId?: number;
  [k: string]: any;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;
  private ACCESS_KEY = 'access_token';
  private REFRESH_KEY = 'refresh_token';

  constructor(private http: HttpClient) {}

  // ---- Auth APIs ----
  login(request: LoginRequest): Observable<BaseResponse<AuthResponse>> {
    return this.http.post<BaseResponse<AuthResponse>>(`${this.apiUrl}/login`, request).pipe(
      tap(res => {
        if (res.data?.accessToken) this.saveToken(res.data.accessToken);
        if (res.data?.refreshToken) this.saveRefreshToken(res.data.refreshToken);
      })
    );
  }

  register(request: RegisterRequest): Observable<BaseResponse<any>> {
    return this.http.post<BaseResponse<any>>(`${this.apiUrl}/register`, request);
  }

  active(request: { email: string; otp: string }): Observable<BaseResponse<any>> {
    return this.http.post<BaseResponse<any>>(`${this.apiUrl}/active`, request);
  }

  refreshToken(): Observable<BaseResponse<AuthResponse>> {
    const refreshToken = this.getRefreshToken();
    if (!refreshToken) return throwError(() => new Error('No refresh token available'));

    return this.http.post<BaseResponse<AuthResponse>>(`${this.apiUrl}/refresh-token`, { refreshToken }).pipe(
      tap(res => {
        if (res.data?.accessToken) this.saveToken(res.data.accessToken);
        if (res.data?.refreshToken) this.saveRefreshToken(res.data.refreshToken);
      })
    );
  }

  logout(): void {
    this.logoutAPI().subscribe({ 
      next: () => {
        this.clearAllTokens();
        // Reload trang để clear toàn bộ state
        window.location.href = '/auth/login';
      }, 
      error: () => {
        this.clearAllTokens();
        window.location.href = '/auth/login';
      }
    });
  }

  private logoutAPI(): Observable<any> {
    const accessToken = this.getToken();
    const refreshToken = this.getRefreshToken();
    if (!accessToken && !refreshToken) return of(null);
    return this.http.post(`${this.apiUrl}/logout`, { accessToken, refreshToken });
  }

  // ---- Token storage (CHỈ dùng localStorage) ----
  getToken(): string | null {
    return localStorage.getItem(this.ACCESS_KEY);
  }
  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_KEY);
  }
  saveToken(token: string) {
    localStorage.setItem(this.ACCESS_KEY, token);
  }
  saveRefreshToken(token: string) {
    localStorage.setItem(this.REFRESH_KEY, token);
  }
  clearToken() {
    localStorage.removeItem(this.ACCESS_KEY);
  }
  clearRefreshToken() {
    localStorage.removeItem(this.REFRESH_KEY);
  }
  private clearAllTokens() {
    this.clearToken();
    this.clearRefreshToken();
  }

  // ---- Helpers dùng cho Guards/FE ----
  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) return false;
    const p = this.decode(token);
    if (!p) return false;
    if (p.exp && p.exp * 1000 < Date.now()) return false; // hết hạn
    return true;
  }

  getRoles(): string[] {
    const token = this.getToken();
    if (!token) return [];
    const p = this.decode(token);
    if (!p) return [];
    if (Array.isArray(p.roles)) return p.roles;
    if (typeof p.roles === 'string') return p.roles.split(',').map(s => s.trim()).filter(Boolean);
    if (typeof p.role === 'string') return [p.role];
    return [];
  }

  getUserRole(): string | null {
    const roles = this.getRoles();
    return roles.length ? roles[0] : null;
  }

  getUserId(): number | null {
    const p = this.decode(this.getToken() || '');
    return p?.userId ?? null;
    // hoặc p?.sub cho username
  }

  private decode(t: string): JwtPayload | null {
    try {
      return jwtDecode<JwtPayload>(t);
    } catch {
      return null;
    }
  }
}
