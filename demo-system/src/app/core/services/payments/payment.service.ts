import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { BaseResponse } from '../../models/response/base-response';
import { CreatePaymentRequest } from '../../models/request/Payment/CreatePaymentRequest';
import { ConfirmPaymentRequest } from '../../models/request/Payment/ConfirmPaymentRequest';
import { PaymentResponse } from '../../models/response/Payment/PaymentResponse';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {

  private apiUrl = `${environment.apiUrl}/payments`;

  constructor(private http: HttpClient) { }

  /**
   * Tạo payment cho order
   */
  createPayment(request: CreatePaymentRequest): Observable<BaseResponse<PaymentResponse>> {
    return this.http.post<BaseResponse<PaymentResponse>>(this.apiUrl, request);
  }

  /**
   * Xác nhận thanh toán (ADMIN)
   */
  confirmPayment(paymentId: number, request: ConfirmPaymentRequest): Observable<BaseResponse<PaymentResponse>> {
    return this.http.patch<BaseResponse<PaymentResponse>>(`${this.apiUrl}/${paymentId}/confirm`, request);
  }

  /**
   * Lấy chi tiết payment
   */
  getPaymentById(paymentId: number): Observable<BaseResponse<PaymentResponse>> {
    return this.http.get<BaseResponse<PaymentResponse>>(`${this.apiUrl}/${paymentId}`);
  }

  /**
   * Lấy lịch sử thanh toán của order
   */
  getOrderPayments(orderId: number): Observable<BaseResponse<PaymentResponse[]>> {
    return this.http.get<BaseResponse<PaymentResponse[]>>(`${this.apiUrl}/order/${orderId}`);
  }

  /**
   * Lấy payment theo transaction ID
   */
  getPaymentByTransaction(transactionId: string): Observable<BaseResponse<PaymentResponse>> {
    return this.http.get<BaseResponse<PaymentResponse>>(`${this.apiUrl}/transaction/${transactionId}`);
  }

  /**
   * Hủy payment
   */
  cancelPayment(paymentId: number): Observable<BaseResponse<PaymentResponse>> {
    return this.http.patch<BaseResponse<PaymentResponse>>(`${this.apiUrl}/${paymentId}/cancel`, {});
  }

  // ========== ADMIN ENDPOINTS ==========

  /**
   * Lấy tất cả payments với phân trang (ADMIN)
   */
  getAllPayments(page: number = 0, size: number = 10, sortBy: string = 'createdAt', sortDirection: string = 'DESC'): Observable<BaseResponse<any>> {
    return this.http.get<BaseResponse<any>>(`${this.apiUrl}/admin/all`, {
      params: { 
        page: page.toString(), 
        size: size.toString(),
        sortBy: sortBy,
        sortDirection: sortDirection
      }
    });
  }

  /**
   * Đếm tổng số thanh toán (ADMIN)
   */
  countAllPayments(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/admin/count`);
  }

  /**
   * Tính tổng doanh thu (ADMIN)
   */
  getTotalRevenue(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/admin/total-revenue`);
  }

  /**
   * Xuất báo cáo thanh toán bất đồng bộ (ADMIN)
   */
  exportPaymentsAsync(status?: string): Observable<BaseResponse<string>> {
    let params = new HttpParams();
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<BaseResponse<string>>(`${environment.apiUrl}/reports/payments/async`, { params });
  }
}
