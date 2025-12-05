import { Component, OnInit } from '@angular/core';

interface OrderItem {
  id: string;
  productName: string;
  productImage: string;
  quantity: number;
  price: number;
  total: number;
}

interface Order {
  id: string;
  orderDate: Date;
  status: string;
  totalAmount: number;
  shippingAddress: string;
  paymentMethod: string;
  items: OrderItem[];
}

@Component({
  selector: 'app-orders',
  templateUrl: './orders.component.html',
  styleUrls: ['./orders.component.css']
})
export class OrdersComponent implements OnInit {
  orders: Order[] = [];
  filteredOrders: Order[] = [];
  isLoading = false;
  selectedStatus = 'all';
  searchTerm = '';

  // Pagination
  currentPage = 1;
  itemsPerPage = 5;
  totalOrders = 0;

  statusOptions = [
    { value: 'all', label: 'Tất cả đơn hàng', count: 0 },
    { value: 'pending', label: 'Chờ xác nhận', count: 0 },
    { value: 'confirmed', label: 'Đã xác nhận', count: 0 },
    { value: 'shipping', label: 'Đang giao', count: 0 },
    { value: 'delivered', label: 'Đã giao', count: 0 },
    { value: 'cancelled', label: 'Đã hủy', count: 0 }
  ];

  constructor() { }

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.isLoading = true;
    
    // Mock data - Replace with actual API call
    setTimeout(() => {
      this.orders = [
        {
          id: 'ORD001',
          orderDate: new Date('2024-01-15'),
          status: 'delivered',
          totalAmount: 1250000,
          shippingAddress: '123 Nguyễn Văn A, Quận 1, TP.HCM',
          paymentMethod: 'Thẻ tín dụng',
          items: [
            {
              id: '1',
              productName: 'iPhone 15 Pro Max',
              productImage: '/assets/products/iphone.jpg',
              quantity: 1,
              price: 1200000,
              total: 1200000
            },
            {
              id: '2',
              productName: 'Ốp lưng iPhone',
              productImage: '/assets/products/case.jpg',
              quantity: 1,
              price: 50000,
              total: 50000
            }
          ]
        },
        {
          id: 'ORD002',
          orderDate: new Date('2024-01-20'),
          status: 'shipping',
          totalAmount: 800000,
          shippingAddress: '456 Lê Văn B, Quận 2, TP.HCM',
          paymentMethod: 'Thanh toán khi nhận hàng',
          items: [
            {
              id: '3',
              productName: 'Samsung Galaxy S24',
              productImage: '/assets/products/samsung.jpg',
              quantity: 1,
              price: 800000,
              total: 800000
            }
          ]
        },
        {
          id: 'ORD003',
          orderDate: new Date('2024-01-25'),
          status: 'pending',
          totalAmount: 300000,
          shippingAddress: '789 Trần Văn C, Quận 3, TP.HCM',
          paymentMethod: 'Chuyển khoản',
          items: [
            {
              id: '4',
              productName: 'Tai nghe Bluetooth',
              productImage: '/assets/products/headphone.jpg',
              quantity: 2,
              price: 150000,
              total: 300000
            }
          ]
        }
      ];

      this.filteredOrders = [...this.orders];
      this.totalOrders = this.orders.length;
      this.updateStatusCounts();
      this.isLoading = false;
    }, 1000);
  }

  updateStatusCounts(): void {
    this.statusOptions.forEach(option => {
      if (option.value === 'all') {
        option.count = this.orders.length;
      } else {
        option.count = this.orders.filter(order => order.status === option.value).length;
      }
    });
  }

  filterOrders(): void {
    let filtered = [...this.orders];

    // Filter by status
    if (this.selectedStatus !== 'all') {
      filtered = filtered.filter(order => order.status === this.selectedStatus);
    }

    // Filter by search term
    if (this.searchTerm.trim()) {
      const searchLower = this.searchTerm.toLowerCase().trim();
      filtered = filtered.filter(order => 
        order.id.toLowerCase().includes(searchLower) ||
        order.items.some(item => item.productName.toLowerCase().includes(searchLower))
      );
    }

    this.filteredOrders = filtered;
    this.totalOrders = filtered.length;
    this.currentPage = 1;
  }

  onStatusChange(): void {
    this.filterOrders();
  }

  onSearchChange(): void {
    this.filterOrders();
  }

  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'pending':
        return 'status-pending';
      case 'confirmed':
        return 'status-confirmed';
      case 'shipping':
        return 'status-shipping';
      case 'delivered':
        return 'status-delivered';
      case 'cancelled':
        return 'status-cancelled';
      default:
        return 'status-pending';
    }
  }

  getStatusText(status: string): string {
    switch (status) {
      case 'pending':
        return 'Chờ xác nhận';
      case 'confirmed':
        return 'Đã xác nhận';
      case 'shipping':
        return 'Đang giao';
      case 'delivered':
        return 'Đã giao';
      case 'cancelled':
        return 'Đã hủy';
      default:
        return 'Không xác định';
    }
  }

  getPaginatedOrders(): Order[] {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    return this.filteredOrders.slice(startIndex, startIndex + this.itemsPerPage);
  }

  getTotalPages(): number {
    return Math.ceil(this.totalOrders / this.itemsPerPage);
  }

  Math = Math;

  goToPage(page: number): void {
    if (page >= 1 && page <= this.getTotalPages()) {
      this.currentPage = page;
    }
  }

  viewOrderDetails(order: Order): void {
    console.log('View order details:', order);
    // TODO: Navigate to order details page or open modal
  }

  trackOrder(order: Order): void {
    console.log('Track order:', order);
    // TODO: Navigate to order tracking page
  }

  reorderItems(order: Order): void {
    console.log('Reorder items:', order);
    // TODO: Add items to cart
  }

  cancelOrder(order: Order): void {
    if (confirm('Bạn có chắc chắn muốn hủy đơn hàng này?')) {
      console.log('Cancel order:', order);
      // TODO: Call API to cancel order
    }
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount);
  }
}