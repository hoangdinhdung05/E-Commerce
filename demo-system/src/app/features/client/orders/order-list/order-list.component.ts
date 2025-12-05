import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { OrderService } from 'src/app/core/services/orders/order.service';
import { OrderResponse } from 'src/app/core/models/response/Order/OrderResponse';
import { OrderStatus, OrderStatusLabels, OrderStatusColors } from 'src/app/utils/OrderStatus';
import { PaymentMethodLabels } from 'src/app/utils/PaymentMethod';
import { PaymentStatusLabels, PaymentStatusColors } from 'src/app/utils/PaymentStatus';

@Component({
  selector: 'app-order-list',
  templateUrl: './order-list.component.html',
  styleUrls: ['./order-list.component.css']
})
export class OrderListComponent implements OnInit {

  orders: OrderResponse[] = [];
  totalPages = 0;
  totalElements = 0;
  currentPage = 0;
  pageSize = 10;
  loading = false;

  selectedStatus?: OrderStatus;
  OrderStatus = OrderStatus;
  OrderStatusLabels = OrderStatusLabels;
  OrderStatusColors = OrderStatusColors;
  PaymentMethodLabels = PaymentMethodLabels;
  PaymentStatusLabels = PaymentStatusLabels;
  PaymentStatusColors = PaymentStatusColors;

  statusOptions = [
    { value: OrderStatus.PENDING, label: OrderStatusLabels[OrderStatus.PENDING] },
    { value: OrderStatus.CONFIRMED, label: OrderStatusLabels[OrderStatus.CONFIRMED] },
    { value: OrderStatus.PROCESSING, label: OrderStatusLabels[OrderStatus.PROCESSING] },
    { value: OrderStatus.SHIPPING, label: OrderStatusLabels[OrderStatus.SHIPPING] },
    { value: OrderStatus.DELIVERED, label: OrderStatusLabels[OrderStatus.DELIVERED] },
    { value: OrderStatus.CANCELLED, label: OrderStatusLabels[OrderStatus.CANCELLED] }
  ];

  constructor(
    private orderService: OrderService,
    private toastr: ToastrService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadMyOrders(this.currentPage);
  }

  loadMyOrders(page: number) {
    this.loading = true;
    this.orderService.getMyOrders(this.selectedStatus, page, this.pageSize).subscribe({
      next: (response) => {
        if (response.success) {
          this.orders = response.data.content;
          this.totalPages = response.data.totalPages;
          this.totalElements = response.data.totalElements;
          this.currentPage = response.data.pageNumber;
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading orders', err);
        this.toastr.error('Không thể tải danh sách đơn hàng!', 'Lỗi');
        this.loading = false;
      }
    });
  }

  filterByStatus() {
    this.currentPage = 0;
    this.loadMyOrders(0);
  }

  clearFilter() {
    this.selectedStatus = undefined;
    this.currentPage = 0;
    this.loadMyOrders(0);
  }

  nextPage() {
    if (this.currentPage < this.totalPages - 1) this.loadMyOrders(this.currentPage + 1);
  }

  previousPage() {
    if (this.currentPage > 0) this.loadMyOrders(this.currentPage - 1);
  }

  goToPage(page: number) {
    this.loadMyOrders(page);
  }

  getPageNumbers(): number[] {
    const maxPagesToShow = 5;
    const pages: number[] = [];

    if (this.totalPages <= maxPagesToShow) {
      for (let i = 0; i < this.totalPages; i++) {
        pages.push(i);
      }
    } else {
      pages.push(0);

      let startPage = Math.max(1, this.currentPage - 1);
      let endPage = Math.min(this.totalPages - 2, this.currentPage + 1);

      if (this.currentPage <= 2) {
        endPage = Math.min(3, this.totalPages - 2);
      }
      if (this.currentPage >= this.totalPages - 3) {
        startPage = Math.max(1, this.totalPages - 4);
      }

      if (startPage > 1) {
        pages.push(-1);
      }

      for (let i = startPage; i <= endPage; i++) {
        pages.push(i);
      }

      if (endPage < this.totalPages - 2) {
        pages.push(-1);
      }

      pages.push(this.totalPages - 1);
    }

    return pages;
  }

  viewOrderDetails(orderId: number) {
    this.router.navigate(['/orders', orderId]);
  }

  cancelOrder(order: OrderResponse) {
    if (!confirm(`Bạn có chắc muốn hủy đơn hàng ${order.orderNumber}?`)) return;

    this.loading = true;
    this.orderService.cancelOrder(order.id).subscribe({
      next: (res) => {
        if (res.success) {
          this.toastr.success('Hủy đơn hàng thành công!', 'Thành công');
          this.loadMyOrders(this.currentPage);
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Cancel order error:', err);
        this.toastr.error(err.error?.message || 'Không thể hủy đơn hàng!', 'Lỗi');
        this.loading = false;
      }
    });
  }

  canCancelOrder(order: OrderResponse): boolean {
    return order.status === OrderStatus.PENDING || order.status === OrderStatus.CONFIRMED;
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount);
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleString('vi-VN');
  }
}

