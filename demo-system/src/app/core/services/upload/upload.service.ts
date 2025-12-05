import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from 'src/environments/environment';
import { UploadResponse } from '../../models/response/System/UploadResponse';
import { BaseResponse } from '../../models/response/base-response';

@Injectable({
  providedIn: 'root'
})
export class UploadService {
  private apiUrl = `${environment.apiUrl}/uploads`;

  constructor(private http: HttpClient) { }

  /**
   * Upload product image
   * @param file Image file to upload
   * @returns UploadResponse containing fileUrl
   */
  uploadProductImage(file: File): Observable<UploadResponse> {
    const formData = new FormData();
    formData.append('file', file);
    
    // Backend có thể trả về BaseResponse<UploadResponse> hoặc trực tiếp UploadResponse
    // Nếu có wrapper BaseResponse thì dùng map để extract data
    return this.http.post<UploadResponse>(`${this.apiUrl}/product`, formData);
  }
}
