import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { takeUntil } from 'rxjs/operators';
import { PaymentService } from '../../../core/services/payments/payment.service';
import { ToastService } from '../../../core/services/toast.service';
import { PaymentResponse } from '../../../core/models/response/Payment/PaymentResponse';
import { PaymentStatus } from '../../../utils/PaymentStatus';
import { DestroyableComponent } from '../../../shared/components/base/destroyable.component';

@Component({
  selector: 'app-payment-result',
  templateUrl: './payment-result.component.html',
  styleUrls: ['./payment-result.component.css']
})
export class PaymentResultComponent extends DestroyableComponent implements OnInit {
  isLoading = true;
  paymentResult: PaymentResponse | null = null;
  isSuccess = false;
  errorMessage = '';

  // Expose enum to template
  PaymentStatus = PaymentStatus;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private paymentService: PaymentService,
    private toastService: ToastService
  ) {
    super();
  }

  ngOnInit(): void {
    this.handleVnPayCallback();
  }

  /**
   * Parse query params từ VNPay và gọi API verify
   */
  private handleVnPayCallback(): void {
    this.route.queryParams
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
        if (Object.keys(params).length === 0) {
          this.errorMessage = 'Không tìm thấy thông tin thanh toán';
          this.isLoading = false;
          return;
        }

        // Gọi API verify payment với VNPay params
        this.paymentService.handleVnPayCallback(params)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: (response) => {
              this.paymentResult = response.data;
              this.isSuccess = this.paymentResult.status === PaymentStatus.PAID;
              this.isLoading = false;

              if (this.isSuccess) {
                this.toastService.success('Thanh toán thành công!');
              } else {
                this.toastService.error('Thanh toán thất bại');
              }
            },
            error: (err) => {
              console.error('Payment verification error:', err);
              this.errorMessage = err.error?.message || 'Không thể xác thực thanh toán';
              this.isLoading = false;
              this.toastService.error(this.errorMessage);
            }
          });
      });
  }

  /**
   * Chuyển đến trang chi tiết đơn hàng
   */
  goToOrderDetail(): void {
    if (this.paymentResult && this.paymentResult.orderNumber) {
      this.router.navigate(['/orders', this.paymentResult.orderNumber]);
    }
  }

  /**
   * Quay về trang chủ
   */
  goToHome(): void {
    this.router.navigate(['/']);
  }

  /**
   * Thử thanh toán lại
   */
  retryPayment(): void {
    if (this.paymentResult) {
      this.router.navigate(['/orders', this.paymentResult.orderNumber]);
    }
  }

  /**
   * Format số tiền
   */
  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount);
  }

  /**
   * Format date
   */
  formatDate(dateString: string): string {
    if (!dateString) return '';
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    }).format(date);
  }
}
