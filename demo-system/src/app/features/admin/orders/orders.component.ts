import { Component, OnInit } from '@angular/core';
import { OrderService } from '../../../core/services/orders/order.service';
import { OrderResponse } from '../../../core/models/response/Order/OrderResponse';
import { OrderStatus } from '../../../utils/OrderStatus';
import { UpdateOrderStatusRequest } from '../../../core/models/request/Order/UpdateOrderStatusRequest';
import { ToastrService } from 'ngx-toastr';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'app-admin-orders',
  templateUrl: './orders.component.html',
  styleUrls: ['./orders.component.css']
})
export class AdminOrdersComponent implements OnInit {
  orders: OrderResponse[] = [];
  isLoading = false;

  imageUrl(product: any) {
    return `${environment.assetBase}${product.productImageUrl}`;
  }
  
  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;
  
  // Filter
  selectedStatus: OrderStatus | undefined = undefined;
  searchSubject = new Subject<string>();
  searchTerm = '';
  
  // Status options
  statusOptions = [
    { value: undefined, label: 'Tất cả trạng thái' },
    { value: OrderStatus.PENDING, label: 'Chờ xác nhận' },
    { value: OrderStatus.CONFIRMED, label: 'Đã xác nhận' },
    { value: OrderStatus.SHIPPING, label: 'Đang giao hàng' },
    { value: OrderStatus.DELIVERED, label: 'Đã giao hàng' },
    { value: OrderStatus.CANCELLED, label: 'Đã hủy' }
  ];
  
  // View state
  selectedOrder: OrderResponse | null = null;
  showDetailModal = false;
  showUpdateStatusModal = false;
  newStatus: OrderStatus = OrderStatus.PENDING;
  
  // Expose to template
  Math = Math;
  OrderStatus = OrderStatus;

  constructor(
    private orderService: OrderService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.loadOrders();
    this.setupSearch();
  }

  setupSearch(): void {
    this.searchSubject.pipe(
      debounceTime(400),
      distinctUntilChanged()
    ).subscribe(searchValue => {
      this.searchTerm = searchValue;
      this.currentPage = 0;
      this.loadOrders();
    });
  }

  loadOrders(): void {
    this.isLoading = true;
    this.orderService.getAllOrders(this.selectedStatus, this.currentPage, this.pageSize).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.orders = response.data.content;
          this.totalPages = response.data.totalPages;
          this.totalElements = response.data.totalElements;
        }
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading orders:', error);
        this.toastr.error('Không thể tải danh sách đơn hàng', 'Lỗi');
        this.isLoading = false;
      }
    });
  }

  onSearchChange(value: string): void {
    this.searchSubject.next(value);
  }

  onStatusFilterChange(): void {
    this.currentPage = 0;
    this.loadOrders();
  }

  viewOrderDetails(order: OrderResponse): void {
    // Admin xem chi tiết order từ data đã load
    this.selectedOrder = order;
    this.showDetailModal = true;
  }

  closeDetailModal(): void {
    this.showDetailModal = false;
    this.selectedOrder = null;
  }

  openUpdateStatusModal(order: OrderResponse): void {
    this.selectedOrder = order;
    this.newStatus = order.status;
    this.showUpdateStatusModal = true;
  }

  closeUpdateStatusModal(): void {
    this.showUpdateStatusModal = false;
    this.selectedOrder = null;
  }

  submitUpdateStatus(): void {
    if (!this.selectedOrder) return;

    const request: UpdateOrderStatusRequest = {
      status: this.newStatus
    };

    this.orderService.updateOrderStatus(this.selectedOrder.id, request).subscribe({
      next: (response) => {
        if (response.success) {
          this.toastr.success('Cập nhật trạng thái đơn hàng thành công', 'Thành công');
          this.closeUpdateStatusModal();
          this.loadOrders();
        }
      },
      error: (error) => {
        console.error('Error updating order status:', error);
        this.toastr.error('Không thể cập nhật trạng thái đơn hàng', 'Lỗi');
      }
    });
  }

  getStatusBadgeClass(status: OrderStatus): string {
    switch (status) {
      case OrderStatus.PENDING:
        return 'badge-warning';
      case OrderStatus.CONFIRMED:
        return 'badge-info';
      case OrderStatus.SHIPPING:
        return 'badge-primary';
      case OrderStatus.DELIVERED:
        return 'badge-success';
      case OrderStatus.CANCELLED:
        return 'badge-danger';
      default:
        return 'badge-secondary';
    }
  }

  getStatusLabel(status: OrderStatus): string {
    switch (status) {
      case OrderStatus.PENDING:
        return 'Chờ xác nhận';
      case OrderStatus.CONFIRMED:
        return 'Đã xác nhận';
      case OrderStatus.SHIPPING:
        return 'Đang giao hàng';
      case OrderStatus.DELIVERED:
        return 'Đã giao hàng';
      case OrderStatus.CANCELLED:
        return 'Đã hủy';
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

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadOrders();
    }
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadOrders();
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadOrders();
    }
  }

  getPageNumbers(): number[] {
    const pages: number[] = [];
    const maxPagesToShow = 5;
    const halfPages = Math.floor(maxPagesToShow / 2);
    
    let startPage = Math.max(0, this.currentPage - halfPages);
    let endPage = Math.min(this.totalPages - 1, startPage + maxPagesToShow - 1);
    
    if (endPage - startPage < maxPagesToShow - 1) {
      startPage = Math.max(0, endPage - maxPagesToShow + 1);
    }
    
    for (let i = startPage; i <= endPage; i++) {
      pages.push(i);
    }
    
    return pages;
  }

  exportOrderPDF(order: OrderResponse): void {
    // Export PDF không còn ở FE nữa, chuyển sang gọi API backend
    this.toastr.info('Đang tạo PDF đơn hàng...', 'Thông báo');
    
    this.orderService.exportOrdersAsync(order.orderNumber).subscribe({
      next: (response) => {
        if (response.success) {
          this.toastr.success('Yêu cầu xuất PDF đã được gửi. Bạn sẽ nhận được email khi file sẵn sàng.', 'Thành công');
        }
      },
      error: (error) => {
        console.error('Error requesting PDF export:', error);
        this.toastr.error('Không thể gửi yêu cầu xuất PDF', 'Lỗi');
      }
    });
  }

  exportOrdersReport(): void {
    this.toastr.info('Đang tạo báo cáo PDF...', 'Thông báo');

    const customerOrder = this.searchTerm.trim() || undefined;
    const status = this.selectedStatus;
    
    this.orderService.exportOrdersAsync(customerOrder, status).subscribe({
      next: (response) => {
        if (response.success) {
          this.toastr.success('Yêu cầu xuất báo cáo PDF đã được gửi. Bạn sẽ nhận được khi file sẵn sàng.', 'Thành công');
        }
      },
      error: (error) => {
        console.error('Error requesting report export:', error);
        this.toastr.error('Không thể gửi yêu cầu xuất báo cáo', 'Lỗi');
      }
    });
  }
}
