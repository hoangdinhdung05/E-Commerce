import { Component, OnInit } from '@angular/core';
import { PaymentService } from '../../../core/services/payments/payment.service';
import { PaymentResponse } from '../../../core/models/response/Payment/PaymentResponse';
import { PaymentStatus } from '../../../utils/PaymentStatus';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-payments',
  templateUrl: './payments.component.html',
  styleUrls: ['./payments.component.css']
})
export class PaymentsComponent implements OnInit {
  payments: PaymentResponse[] = [];
  filteredPayments: PaymentResponse[] = [];
  loading = false;
  
  // Search
  searchText = '';
  
  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;
  
  // Sorting
  sortBy = 'createdAt';
  sortDirection = 'DESC';

  constructor(
    private paymentService: PaymentService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.loadPayments();
  }

  loadPayments(): void {
    this.loading = true;
    this.paymentService.getAllPayments(this.currentPage, this.pageSize, this.sortBy, this.sortDirection)
      .subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.payments = response.data.content;
            this.filteredPayments = [...this.payments];
            this.totalElements = response.data.totalElements;
            this.totalPages = response.data.totalPages;
            this.currentPage = response.data.number;
          }
          this.loading = false;
        },
        error: (error) => {
          console.error('Error loading payments:', error);
          this.toastr.error('Không thể tải danh sách thanh toán', 'Lỗi');
          this.loading = false;
        }
      });
  }

  filterPayments(): void {
    if (!this.searchText) {
      this.filteredPayments = [...this.payments];
      return;
    }
    
    const searchLower = this.searchText.toLowerCase();
    this.filteredPayments = this.payments.filter(payment => 
      payment.transactionId?.toLowerCase().includes(searchLower) ||
      payment.id.toString().includes(searchLower) ||
      payment.orderId.toString().includes(searchLower)
    );
  }

  exportPaymentReport(): void {
    this.toastr.info('Đang tạo báo cáo thanh toán...', 'Thông báo');
    
    this.paymentService.exportPaymentsAsync().subscribe({
      next: (response) => {
        if (response.success) {
          this.toastr.success('Yêu cầu xuất báo cáo đã được gửi. Bạn sẽ nhận được email khi file sẵn sàng.', 'Thành công');
        }
      },
      error: (error) => {
        console.error('Error requesting payment report export:', error);
        this.toastr.error('Không thể gửi yêu cầu xuất báo cáo', 'Lỗi');
      }
    });
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadPayments();
  }

  onPageSizeChange(size: number): void {
    this.pageSize = size;
    this.currentPage = 0;
    this.loadPayments();
  }

  onSort(column: string): void {
    if (this.sortBy === column) {
      this.sortDirection = this.sortDirection === 'ASC' ? 'DESC' : 'ASC';
    } else {
      this.sortBy = column;
      this.sortDirection = 'DESC';
    }
    this.loadPayments();
  }

  confirmPayment(payment: PaymentResponse): void {
    if (payment.status !== PaymentStatus.PENDING) {
      this.toastr.warning('Chỉ có thể xác nhận thanh toán đang chờ', 'Cảnh báo');
      return;
    }

    if (!confirm(`Xác nhận thanh toán #${payment.id}?`)) {
      return;
    }

    const confirmRequest = {
      status: PaymentStatus.PAID,
      transactionId: payment.transactionId || `TXN-${Date.now()}`,
      paymentInfo: 'Xác nhận bởi Admin'
    };

    this.paymentService.confirmPayment(payment.id, confirmRequest)
      .subscribe({
        next: (response) => {
          if (response.success) {
            this.toastr.success('Xác nhận thanh toán thành công', 'Thành công');
            this.loadPayments();
          }
        },
        error: (error) => {
          console.error('Error confirming payment:', error);
          this.toastr.error('Không thể xác nhận thanh toán', 'Lỗi');
        }
      });
  }

  markPaymentFailed(payment: PaymentResponse): void {
    const errorMessage = prompt('Nhập lý do thanh toán thất bại (tùy chọn):');
    if (errorMessage === null) return; // User cancelled

    const confirmRequest = {
      status: PaymentStatus.FAILED,
      errorMessage: errorMessage || 'Thanh toán thất bại',
      paymentInfo: 'Đánh dấu thất bại bởi Admin'
    };

    this.paymentService.confirmPayment(payment.id, confirmRequest)
      .subscribe({
        next: (response) => {
          if (response.success) {
            this.toastr.warning('Đã đánh dấu thanh toán thất bại', 'Thành công');
            this.loadPayments();
          }
        },
        error: (error) => {
          console.error('Error marking payment as failed:', error);
          this.toastr.error('Không thể cập nhật trạng thái', 'Lỗi');
        }
      });
  }

  refundPayment(payment: PaymentResponse): void {
    if (payment.status !== PaymentStatus.PAID) {
      this.toastr.warning('Chỉ có thể hoàn tiền cho thanh toán đã thanh toán', 'Cảnh báo');
      return;
    }

    if (!confirm(`Xác nhận hoàn tiền cho thanh toán #${payment.id}?`)) {
      return;
    }

    const confirmRequest = {
      status: PaymentStatus.REFUNDED,
      paymentInfo: 'Hoàn tiền bởi Admin'
    };

    this.paymentService.confirmPayment(payment.id, confirmRequest)
      .subscribe({
        next: (response) => {
          if (response.success) {
            this.toastr.info('Đã hoàn tiền thành công', 'Thành công');
            this.loadPayments();
          }
        },
        error: (error) => {
          console.error('Error refunding payment:', error);
          this.toastr.error('Không thể hoàn tiền', 'Lỗi');
        }
      });
  }

  getStatusBadgeClass(status: PaymentStatus): string {
    switch (status) {
      case PaymentStatus.PENDING:
        return 'badge-warning';
      case PaymentStatus.PAID:
        return 'badge-success';
      case PaymentStatus.FAILED:
        return 'badge-danger';
      case PaymentStatus.REFUNDED:
        return 'badge-info';
      default:
        return 'badge-secondary';
    }
  }

  getStatusLabel(status: PaymentStatus): string {
    switch (status) {
      case PaymentStatus.PENDING:
        return 'Chờ thanh toán';
      case PaymentStatus.PAID:
        return 'Đã thanh toán';
      case PaymentStatus.FAILED:
        return 'Thất bại';
      case PaymentStatus.REFUNDED:
        return 'Đã hoàn tiền';
      default:
        return 'Không xác định';
    }
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleString('vi-VN');
  }

  getPageNumbers(): number[] {
    const pages: number[] = [];
    const maxPagesToShow = 5;
    let startPage = Math.max(0, this.currentPage - Math.floor(maxPagesToShow / 2));
    let endPage = Math.min(this.totalPages - 1, startPage + maxPagesToShow - 1);

    if (endPage - startPage < maxPagesToShow - 1) {
      startPage = Math.max(0, endPage - maxPagesToShow + 1);
    }

    for (let i = startPage; i <= endPage; i++) {
      pages.push(i);
    }
    return pages;
  }
}
