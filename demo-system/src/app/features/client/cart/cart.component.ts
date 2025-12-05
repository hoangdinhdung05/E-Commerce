import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CartService } from 'src/app/core/services/cart/cart.service';
import { CartResponse } from 'src/app/core/models/response/Cart/CartResponse';
import { CartItemResponse } from 'src/app/core/models/response/Cart/CartItemResponse';
import { ToastService } from 'src/app/core/services/toast.service';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'app-cart',
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.css']
})
export class CartComponent implements OnInit {
  cart: CartResponse | null = null;
  isLoading = false;
  updatingItems: Set<number> = new Set();

  constructor(
    private cartService: CartService,
    private router: Router,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    // Subscribe to cart observable instead of loading manually
    this.cartService.cart$.subscribe({
      next: (cart) => {
        this.cart = cart;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading cart:', err);
        this.isLoading = false;
      }
    });
  }

  getProductImageUrl(item: CartItemResponse): string {
    if (!item.productImageUrl) {
      return 'https://via.placeholder.com/100x100/007bff/ffffff?text=No+Image';
    }
    return `${environment.assetBase}${item.productImageUrl.startsWith('/') ? '' : '/'}${item.productImageUrl}`;
  }

  updateQuantity(item: CartItemResponse, newQuantity: number): void {
    if (newQuantity < 1) return;
    if (newQuantity > item.availableStock) {
      this.toastService.warning(`Chỉ còn ${item.availableStock} sản phẩm trong kho`);
      return;
    }

    this.updatingItems.add(item.id);
    this.cartService.updateCartItem(item.id, { quantity: newQuantity }).subscribe({
      next: (response) => {
        if (response.success) {
          this.toastService.success('Đã cập nhật số lượng');
        }
        this.updatingItems.delete(item.id);
      },
      error: (err) => {
        console.error('Error updating cart item:', err);
        this.toastService.error('Không thể cập nhật số lượng');
        this.updatingItems.delete(item.id);
      }
    });
  }

  increaseQuantity(item: CartItemResponse): void {
    this.updateQuantity(item, item.quantity + 1);
  }

  decreaseQuantity(item: CartItemResponse): void {
    this.updateQuantity(item, item.quantity - 1);
  }

  removeItem(item: CartItemResponse): void {
    if (!confirm(`Bạn có chắc muốn xóa "${item.productName}" khỏi giỏ hàng?`)) {
      return;
    }

    this.updatingItems.add(item.id);
    this.cartService.removeCartItem(item.id).subscribe({
      next: (response) => {
        if (response.success) {
          this.toastService.success('Đã xóa sản phẩm khỏi giỏ hàng');
        }
        this.updatingItems.delete(item.id);
      },
      error: (err) => {
        console.error('Error removing cart item:', err);
        this.toastService.error('Không thể xóa sản phẩm');
        this.updatingItems.delete(item.id);
      }
    });
  }

  clearCart(): void {
    if (!this.cart || this.cart.totalItems === 0) return;
    
    if (!confirm('Bạn có chắc muốn xóa tất cả sản phẩm trong giỏ hàng?')) {
      return;
    }

    this.isLoading = true;
    this.cartService.clearCart().subscribe({
      next: () => {
        this.cart = null;
        this.toastService.success('Đã xóa toàn bộ giỏ hàng');
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error clearing cart:', err);
        this.toastService.error('Không thể xóa giỏ hàng');
        this.isLoading = false;
      }
    });
  }

  proceedToCheckout(): void {
    if (!this.cart || this.cart.totalItems === 0) {
      this.toastService.warning('Giỏ hàng trống');
      return;
    }
    this.router.navigate(['/checkout']);
  }

  continueShopping(): void {
    this.router.navigate(['/category']);
  }

  getTotalAmount(): number {
    return this.cart?.totalAmount || 0;
  }

  isUpdating(itemId: number): boolean {
    return this.updatingItems.has(itemId);
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('vi-VN', { 
      style: 'currency', 
      currency: 'VND' 
    }).format(amount);
  }
}
