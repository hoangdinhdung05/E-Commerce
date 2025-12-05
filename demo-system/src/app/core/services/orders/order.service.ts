import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { BaseResponse } from '../../models/response/base-response';
import { PageResponse } from '../../models/response/page-response';
import { CreateOrderRequest } from '../../models/request/Order/CreateOrderRequest';
import { CheckoutCartRequest } from '../../models/request/Order/CheckoutCartRequest';
import { UpdateOrderStatusRequest } from '../../models/request/Order/UpdateOrderStatusRequest';
import { OrderResponse } from '../../models/response/Order/OrderResponse';
import { OrderStatus } from 'src/app/utils/OrderStatus';

@Injectable({
  providedIn: 'root'
})
export class OrderService {

  private apiUrl = `${environment.apiUrl}/orders`;

  constructor(private http: HttpClient) { }

  /**
   * Tạo đơn hàng mới
   */
  createOrder(request: CreateOrderRequest): Observable<BaseResponse<OrderResponse>> {
    return this.http.post<BaseResponse<OrderResponse>>(this.apiUrl, request);
  }

  /**
   * Checkout từ giỏ hàng
   */
  checkoutCart(request: CheckoutCartRequest): Observable<BaseResponse<OrderResponse>> {
    return this.http.post<BaseResponse<OrderResponse>>(`${this.apiUrl}/checkout`, request);
  }

  /**
   * Lấy danh sách đơn hàng của user hiện tại
   */
  getMyOrders(status?: OrderStatus, pageNumber: number = 0, pageSize: number = 10): Observable<BaseResponse<PageResponse<OrderResponse>>> {
    let params = new HttpParams()
      .set('page', String(pageNumber))
      .set('size', String(pageSize));
    
    if (status) {
      params = params.set('status', status);
    }

    return this.http.get<BaseResponse<PageResponse<OrderResponse>>>(`${this.apiUrl}/my-orders`, { params });
  }

  /**
   * Lấy chi tiết đơn hàng
   */
  getOrderById(orderId: number): Observable<BaseResponse<OrderResponse>> {
    return this.http.get<BaseResponse<OrderResponse>>(`${this.apiUrl}/${orderId}`);
  }

  /**
   * Lấy đơn hàng theo order number
   */
  getOrderByNumber(orderNumber: string): Observable<BaseResponse<OrderResponse>> {
    return this.http.get<BaseResponse<OrderResponse>>(`${this.apiUrl}/number/${orderNumber}`);
  }

  /**
   * Hủy đơn hàng
   */
  cancelOrder(orderId: number): Observable<BaseResponse<OrderResponse>> {
    return this.http.patch<BaseResponse<OrderResponse>>(`${this.apiUrl}/${orderId}/cancel`, {});
  }

  /**
   * Đếm số đơn hàng của user
   */
  countMyOrders(): Observable<BaseResponse<number>> {
    return this.http.get<BaseResponse<number>>(`${this.apiUrl}/count`);
  }

  // ========== ADMIN ENDPOINTS ==========

  /**
   * Đếm tổng số đơn hàng (ADMIN)
   */
  countAllOrders(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/admin/count`);
  }

  /**
   * Đếm đơn hàng theo trạng thái (ADMIN)
   */
  countOrdersByStatus(status: OrderStatus): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/admin/count/status/${status}`);
  }

  /**
   * Lấy tất cả đơn hàng (ADMIN)
   */
  getAllOrders(status?: OrderStatus, pageNumber: number = 0, pageSize: number = 20): Observable<BaseResponse<PageResponse<OrderResponse>>> {
    let params = new HttpParams()
      .set('page', String(pageNumber))
      .set('size', String(pageSize));
    
    if (status) {
      params = params.set('status', status);
    }

    return this.http.get<BaseResponse<PageResponse<OrderResponse>>>(`${this.apiUrl}/admin/all`, { params });
  }

  /**
   * Cập nhật trạng thái đơn hàng (ADMIN)
   */
  updateOrderStatus(orderId: number, request: UpdateOrderStatusRequest): Observable<BaseResponse<OrderResponse>> {
    return this.http.patch<BaseResponse<OrderResponse>>(`${this.apiUrl}/admin/${orderId}/status`, request);
  }

  /**
   * Xuất báo cáo đơn hàng bất đồng bộ (ADMIN)
   */
  exportOrdersAsync(orderNumber?: string, status?: string): Observable<BaseResponse<string>> {
    let params = new HttpParams();
    if (orderNumber) {
      params = params.set('orderNumber', orderNumber);
    }
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<BaseResponse<string>>(`${environment.apiUrl}/reports/orders/async`, { params });
  }
}
