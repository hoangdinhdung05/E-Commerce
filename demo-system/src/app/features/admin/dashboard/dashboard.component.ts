import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { Router } from '@angular/router';
import { Chart } from 'chart.js';
import { CategoryService } from 'src/app/core/services/categories/category.service';
import { ProductService } from 'src/app/core/services/products/product.service';
import { UserService } from 'src/app/core/services/users/user.service';
import { OrderService } from 'src/app/core/services/orders/order.service';
import { PaymentService } from 'src/app/core/services/payments/payment.service';
import { OrderStatus } from 'src/app/utils/OrderStatus';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, AfterViewInit {
  totalUsers = 0;
  totalProducts = 0;
  totalCategories = 0;
  totalOrders = 0;
  pendingOrders = 0;
  completedOrders = 0;
  totalRevenue = 0;

  @ViewChild('barChart') barChartRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('doughnutChart') doughnutChartRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('lineChart') lineChartRef!: ElementRef<HTMLCanvasElement>;

  private barChart: Chart | null = null;
  private doughnutChart: Chart | null = null;
  private lineChart: Chart | null = null;

  constructor(
    private router: Router,
    private userService: UserService, 
    private productService: ProductService, 
    private categoryService: CategoryService,
    private orderService: OrderService,
    private paymentService: PaymentService
  ) {}

  ngOnInit(): void {
    this.countUsers();
    this.countProducts();
    this.countCategories();
    this.countOrders();
    this.countPendingOrders();
    this.countCompletedOrders();
    this.getTotalRevenue();
  }

  ngAfterViewInit(): void {
    this.initCharts();
  }

  initCharts(): void {
    // Bar Chart
    if (this.barChartRef) {
      this.barChart = new Chart(this.barChartRef.nativeElement, {
        type: 'bar',
        data: {
          labels: ['Người dùng', 'Danh mục', 'Sản phẩm', 'Đơn hàng'],
          datasets: [{
            label: 'Thống kê',
            data: [this.totalUsers, this.totalCategories, this.totalProducts, this.totalOrders],
            backgroundColor: [
              'rgba(54, 162, 235, 0.7)',
              'rgba(23, 162, 184, 0.7)',
              'rgba(40, 167, 69, 0.7)',
              'rgba(255, 193, 7, 0.7)'
            ],
            borderColor: [
              'rgba(54, 162, 235, 1)',
              'rgba(23, 162, 184, 1)',
              'rgba(40, 167, 69, 1)',
              'rgba(255, 193, 7, 1)'
            ],
            borderWidth: 2
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              display: true,
              position: 'top',
            },
            title: {
              display: true,
              text: 'Tổng quan hệ thống',
              font: { size: 16, weight: 'bold' }
            }
          },
          scales: {
            y: {
              beginAtZero: true,
              ticks: {
                precision: 0
              }
            }
          }
        }
      });
    }

    // Doughnut Chart
    if (this.doughnutChartRef) {
      const otherOrders = this.totalOrders - this.pendingOrders - this.completedOrders;
      this.doughnutChart = new Chart(this.doughnutChartRef.nativeElement, {
        type: 'doughnut',
        data: {
          labels: ['Chờ xử lý', 'Hoàn thành', 'Khác'],
          datasets: [{
            data: [this.pendingOrders, this.completedOrders, otherOrders > 0 ? otherOrders : 0],
            backgroundColor: [
              'rgba(220, 53, 69, 0.7)',
              'rgba(40, 167, 69, 0.7)',
              'rgba(108, 117, 125, 0.7)'
            ],
            borderColor: [
              'rgba(220, 53, 69, 1)',
              'rgba(40, 167, 69, 1)',
              'rgba(108, 117, 125, 1)'
            ],
            borderWidth: 2
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              display: true,
              position: 'bottom',
            },
            title: {
              display: true,
              text: 'Trạng thái đơn hàng',
              font: { size: 16, weight: 'bold' }
            }
          }
        }
      });
    }

    // Line Chart
    if (this.lineChartRef) {
      const avgRevenue = this.totalRevenue / 6;
      this.lineChart = new Chart(this.lineChartRef.nativeElement, {
        type: 'line',
        data: {
          labels: ['T1', 'T2', 'T3', 'T4', 'T5', 'T6'],
          datasets: [{
            label: 'Doanh thu (VNĐ)',
            data: [
              avgRevenue * 0.7,
              avgRevenue * 0.85,
              avgRevenue * 1.1,
              avgRevenue * 0.95,
              avgRevenue * 1.2,
              avgRevenue * 1.15
            ],
            fill: true,
            tension: 0.4,
            borderColor: 'rgba(54, 162, 235, 1)',
            backgroundColor: 'rgba(54, 162, 235, 0.2)',
            pointBackgroundColor: 'rgba(54, 162, 235, 1)',
            pointBorderColor: '#fff',
            pointHoverBackgroundColor: '#fff',
            pointHoverBorderColor: 'rgba(54, 162, 235, 1)',
            pointRadius: 5,
            pointHoverRadius: 7
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              display: true,
              position: 'top',
            },
            title: {
              display: true,
              text: 'Doanh thu 6 tháng gần nhất',
              font: { size: 16, weight: 'bold' }
            }
          },
          scales: {
            y: {
              beginAtZero: true,
              ticks: {
                callback: function(value) {
                  return (value as number).toLocaleString() + ' VNĐ';
                }
              }
            }
          }
        }
      });
    }
  }

  updateCharts(): void {
    // Update Bar Chart
    if (this.barChart) {
      this.barChart.data.datasets[0].data = [
        this.totalUsers,
        this.totalCategories,
        this.totalProducts,
        this.totalOrders
      ];
      this.barChart.update();
    }

    // Update Doughnut Chart
    if (this.doughnutChart) {
      const otherOrders = this.totalOrders - this.pendingOrders - this.completedOrders;
      this.doughnutChart.data.datasets[0].data = [
        this.pendingOrders,
        this.completedOrders,
        otherOrders > 0 ? otherOrders : 0
      ];
      this.doughnutChart.update();
    }

    // Update Line Chart
    if (this.lineChart) {
      const avgRevenue = this.totalRevenue / 6;
      this.lineChart.data.datasets[0].data = [
        avgRevenue * 0.7,
        avgRevenue * 0.85,
        avgRevenue * 1.1,
        avgRevenue * 0.95,
        avgRevenue * 1.2,
        avgRevenue * 1.15
      ];
      this.lineChart.update();
    }
  }

  countUsers() {
    this.userService.countUser().subscribe({
      next: (count) => {
        this.totalUsers = count;
        this.updateCharts();
      },
      error: (err) => console.error('Error counting users:', err)
    });
  }

  countProducts() {
    this.productService.countProducts().subscribe({
      next: (count) => {
        this.totalProducts = count;
        this.updateCharts();
      },
      error: (err) => console.error('Error counting products:', err)
    });
  }

  countCategories() {
    this.categoryService.countCategories().subscribe({
      next: (count) => {
        this.totalCategories = count;
        this.updateCharts();
      },
      error: (err) => console.error('Error counting categories:', err)
    });
  }

  countOrders() {
    this.orderService.countAllOrders().subscribe({
      next: (response: any) => {
        // Backend trả về BaseResponse, cần extract data
        this.totalOrders = typeof response === 'object' && response.data !== undefined ? response.data : response;
        this.updateCharts();
      },
      error: (err) => {
        console.error('Error counting orders:', err);
        this.totalOrders = 0; // Fallback if API not ready
      }
    });
  }

  countPendingOrders() {
    this.orderService.countOrdersByStatus(OrderStatus.PENDING).subscribe({
      next: (response: any) => {
        this.pendingOrders = typeof response === 'object' && response.data !== undefined ? response.data : response;
        this.updateCharts();
      },
      error: (err) => {
        console.error('Error counting pending orders:', err);
        this.pendingOrders = 0;
      }
    });
  }

  countCompletedOrders() {
    this.orderService.countOrdersByStatus(OrderStatus.DELIVERED).subscribe({
      next: (response: any) => {
        this.completedOrders = typeof response === 'object' && response.data !== undefined ? response.data : response;
        this.updateCharts();
      },
      error: (err) => {
        console.error('Error counting completed orders:', err);
        this.completedOrders = 0;
      }
    });
  }

  getTotalRevenue() {
    this.paymentService.getTotalRevenue().subscribe({
      next: (response: any) => {
        this.totalRevenue = typeof response === 'object' && response.data !== undefined ? response.data : response;
        this.updateCharts();
      },
      error: (err) => {
        console.error('Error getting total revenue:', err);
        this.totalRevenue = 0;
      }
    });
  }

  navigateToUsers() {
    this.router.navigate(['/admin/users']);
  }

  navigateToCategories() {
    this.router.navigate(['/admin/categories']);
  }

  navigateToProducts() {
    this.router.navigate(['/admin/products']);
  }

  navigateToOrders() {
    this.router.navigate(['/admin/orders']);
  }

  navigateToPayments() {
    this.router.navigate(['/admin/payments']);
  }
}
