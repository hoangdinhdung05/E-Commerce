import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { BaseResponse } from '../../models/response/base-response';
import { CategoryResponse } from '../../models/response/Category/CategoryResponse';
import { PageResponse } from '../../models/response/page-response';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { CategoryRequest } from '../../models/request/Category/CategoryRequest';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {

  private apiUrl = `${environment.apiUrl}/categories`;

  constructor(private http: HttpClient) { }

  getAllCategories(pageNumber: number, pageSize: number): Observable<BaseResponse<PageResponse<CategoryResponse>>> {
    const params = new HttpParams()
      .set('pageNumber', pageNumber)
      .set('pageSize', pageSize);
    
    return this.http.get<BaseResponse<PageResponse<CategoryResponse>>>(this.apiUrl, { params });
  }

  getCategoryDetails(id: number): Observable<BaseResponse<CategoryResponse>> {
    return this.http.get<BaseResponse<CategoryResponse>>(`${this.apiUrl}/details/${id}`);
  }

  adminCreateCategory(request: CategoryRequest): Observable<BaseResponse<any>> {
    return this.http.post<BaseResponse<any>>(`${this.apiUrl}/add`, request);
  } 

  updateCategory(id: number, request: CategoryRequest): Observable<BaseResponse<any>> {
    return this.http.patch<BaseResponse<any>>(`${this.apiUrl}/${id}`, request);
  }

  deleteCategory(id: number): Observable<BaseResponse<any>> {
    return this.http.delete<BaseResponse<any>>(`${this.apiUrl}/${id}`);
  }

  countCategories(): Observable<number> {
    return this.http.get<BaseResponse<number>>(`${this.apiUrl}/count`)
      .pipe(map(res => res.data));
  }

  /**
   * Export categories report as a Blob (e.g., PDF)
   */
  exportCategoryReport(): Observable<Blob> {
    return this.http.get(`${environment.apiUrl}/reports/categories`, {
      responseType: 'blob'
    });
  }
}
