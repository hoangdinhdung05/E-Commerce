import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from 'src/environments/environment';
import { BaseResponse } from '../../models/response/base-response';
import { CartResponse } from '../../models/response/Cart/CartResponse';
import { AddToCartRequest } from '../../models/request/Cart/AddToCartRequest';
import { UpdateCartItemRequest } from '../../models/request/Cart/UpdateCartItemRequest';
import { AuthService } from '../../auth.service';

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private apiUrl = `${environment.apiUrl}/cart`;
  private cartSubject = new BehaviorSubject<CartResponse | null>(null);
  public cart$ = this.cartSubject.asObservable();
  
  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {
    // Chỉ load cart nếu user đã đăng nhập
    if (this.authService.isAuthenticated()) {
      this.loadCart();
    }
  }

  /**
   * Tải giỏ hàng từ server
   */
  private loadCart(): void {
    // Kiểm tra lại trước khi load
    if (!this.authService.isAuthenticated()) {
      this.cartSubject.next(null);
      return;
    }

    this.getCart().subscribe({
      next: (response) => {
        this.cartSubject.next(response.data);
      },
      error: (err) => {
        console.error('Error loading cart:', err);
        this.cartSubject.next(null);
      }
    });
  }

  /**
   * Lấy giỏ hàng của user hiện tại
   */
  getCart(): Observable<BaseResponse<CartResponse>> {
    return this.http.get<BaseResponse<CartResponse>>(this.apiUrl).pipe(
      tap(response => this.cartSubject.next(response.data))
    );
  }

  /**
   * Thêm sản phẩm vào giỏ hàng
   */
  addToCart(request: AddToCartRequest): Observable<BaseResponse<CartResponse>> {
    return this.http.post<BaseResponse<CartResponse>>(`${this.apiUrl}/add`, request).pipe(
      tap(response => this.cartSubject.next(response.data))
    );
  }

  /**
   * Cập nhật số lượng item trong giỏ
   */
  updateCartItem(cartItemId: number, request: UpdateCartItemRequest): Observable<BaseResponse<CartResponse>> {
    return this.http.put<BaseResponse<CartResponse>>(`${this.apiUrl}/items/${cartItemId}`, request).pipe(
      tap(response => this.cartSubject.next(response.data))
    );
  }

  /**
   * Xóa item khỏi giỏ hàng
   */
  removeCartItem(cartItemId: number): Observable<BaseResponse<CartResponse>> {
    return this.http.delete<BaseResponse<CartResponse>>(`${this.apiUrl}/items/${cartItemId}`).pipe(
      tap(response => this.cartSubject.next(response.data))
    );
  }

  /**
   * Xóa toàn bộ giỏ hàng
   */
  clearCart(): Observable<BaseResponse<string>> {
    return this.http.delete<BaseResponse<string>>(`${this.apiUrl}/clear`).pipe(
      tap(() => this.cartSubject.next(null))
    );
  }

  /**
   * Đếm số lượng items trong giỏ
   */
  getCartItemCount(): Observable<BaseResponse<number>> {
    return this.http.get<BaseResponse<number>>(`${this.apiUrl}/count`);
  }

  /**
   * Lấy tổng số items trong giỏ (từ state hiện tại)
   */
  getCurrentCartCount(): number {
    const cart = this.cartSubject.value;
    return cart?.totalItems || 0;
  }

  /**
   * Lấy tổng giá trị giỏ hàng (từ state hiện tại)
   */
  getCurrentCartTotal(): number {
    const cart = this.cartSubject.value;
    return cart?.totalAmount || 0; // Changed from totalPrice to totalAmount
  }

  /**
   * Refresh cart data
   */
  refreshCart(): void {
    if (this.authService.isAuthenticated()) {
      this.loadCart();
    } else {
      this.cartSubject.next(null);
    }
  }

  /**
   * Clear local cart state (dùng khi logout)
   */
  clearLocalCart(): void {
    this.cartSubject.next(null);
  }
}
