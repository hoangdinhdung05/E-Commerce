import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { CartService } from '../../../core/services/cart/cart.service';
import { OrderService } from '../../../core/services/orders/order.service';
import { ToastService } from '../../../core/services/toast.service';
import { CartResponse } from '../../../core/models/response/Cart/CartResponse';
import { CartItemResponse } from '../../../core/models/response/Cart/CartItemResponse';
import { CheckoutCartRequest } from '../../../core/models/request/Order/CheckoutCartRequest';
import { PaymentMethod } from '../../../utils/PaymentMethod';
import { ReactiveFormsModule } from '@angular/forms';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-checkout',
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.css']
})
export class CheckoutComponent implements OnInit {
  cart: CartResponse | null = null;
  checkoutForm!: FormGroup;
  isLoading = false;
  isSubmitting = false;

  // Payment methods enum for template
  paymentMethods = [
    { value: PaymentMethod.COD, label: 'Thanh toán khi nhận hàng (COD)' },
    { value: PaymentMethod.BANK_TRANSFER, label: 'Chuyển khoản ngân hàng' },
    { value: PaymentMethod.VNPAY, label: 'VNPay' },
    { value: PaymentMethod.MOMO, label: 'MoMo' },
    { value: PaymentMethod.CREDIT_CARD, label: 'Thẻ tín dụng/ghi nợ' }
  ];

  constructor(
    private fb: FormBuilder,
    private cartService: CartService,
    private orderService: OrderService,
    private toastService: ToastService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadCart();
  }

  /**
   * Initialize checkout form
   */
  private initForm(): void {
    this.checkoutForm = this.fb.group({
      receiverName: ['', [Validators.required, Validators.maxLength(200)]],
      phoneNumber: ['', [
        Validators.required, 
        Validators.pattern(/^[0-9]{10,11}$/)
      ]],
      shippingAddress: ['', [Validators.required, Validators.maxLength(500)]],
      paymentMethod: [PaymentMethod.COD, Validators.required],
      note: ['', Validators.maxLength(1000)]
    });
  }

  /**
   * Load cart data
   */
  private loadCart(): void {
    this.isLoading = true;
    
    this.cartService.cart$.subscribe({
      next: (cart) => {
        this.cart = cart;
        this.isLoading = false;

        // Redirect if cart is empty
        if (!cart || cart.totalItems === 0) {
          this.toastService.warning('Giỏ hàng trống. Vui lòng thêm sản phẩm vào giỏ hàng');
          this.router.navigate(['/cart']);
        }
      },
      error: (err) => {
        console.error('Error loading cart:', err);
        this.isLoading = false;
        this.toastService.error('Không thể tải giỏ hàng');
      }
    });
  }

  /**
   * Handle checkout form submission
   */
  onSubmit(): void {
    if (this.checkoutForm.invalid) {
      this.markFormGroupTouched(this.checkoutForm);
      this.toastService.warning('Vui lòng điền đầy đủ thông tin');
      return;
    }

    if (!this.cart || this.cart.totalItems === 0) {
      this.toastService.error('Giỏ hàng trống');
      return;
    }

    this.isSubmitting = true;
    const request: CheckoutCartRequest = this.checkoutForm.value;

    this.orderService.checkoutCart(request).subscribe({
      next: (response) => {
        this.toastService.success(
          `Đặt hàng thành công! Mã đơn hàng: ${response.data.orderNumber}`
        );
        
        // Clear cart
        this.cartService.refreshCart();
        
        // Redirect to order detail or success page
        this.router.navigate(['/orders', response.data.orderNumber]);
      },
      error: (err) => {
        console.error('Checkout error:', err);
        const errorMessage = err.error?.message || 'Đã có lỗi xảy ra khi đặt hàng';
        this.toastService.error(errorMessage);
        this.isSubmitting = false;
      }
    });
  }

  /**
   * Mark all form fields as touched to show validation errors
   */
  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      control?.markAsTouched();
      if (control instanceof FormGroup) {
        this.markFormGroupTouched(control);
      }
    });
  }

  /**
   * Get total amount from cart
   */
  getTotalAmount(): number {
    return this.cart?.totalAmount || 0;
  }

  /**
   * Check if form field has error
   */
  hasError(fieldName: string, errorType: string): boolean {
    const field = this.checkoutForm.get(fieldName);
    return !!(field && field.hasError(errorType) && field.touched);
  }

  /**
   * Navigate back to cart
   */
  backToCart(): void {
    this.router.navigate(['/cart']);
  }

  /**
   * Format currency to VND
   */
  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('vi-VN', { 
      style: 'currency', 
      currency: 'VND' 
    }).format(amount);
  }

  /**
   * Get product image URL with proper base path
   */
  getProductImageUrl(item: CartItemResponse): string {
    if (!item.productImageUrl) {
      return 'https://via.placeholder.com/100x100/0b6ff0/ffffff?text=No+Image';
    }
    return `${environment.assetBase}${item.productImageUrl.startsWith('/') ? '' : '/'}${item.productImageUrl}`;
  }
}
