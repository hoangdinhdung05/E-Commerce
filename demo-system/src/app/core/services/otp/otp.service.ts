import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { BaseResponse } from '../../models/response/base-response';
import { ResendOtpRequest } from '../../models/request/Otp/resend-otp-request';

@Injectable({
  providedIn: 'root'
})
export class OtpService {
  private apiUrl = `${environment.apiUrl}/otp`;

  constructor(private http: HttpClient) {}

  /**
   * Resend OTP to user's email
   * @param request ResendOtpRequest containing email and type
   * @returns Observable of BaseResponse
   */
  resendOtp(request: ResendOtpRequest): Observable<BaseResponse<any>> {
    return this.http.post<BaseResponse<any>>(`${this.apiUrl}/resend`, request);
  }
}
