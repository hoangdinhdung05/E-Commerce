import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { BaseResponse } from '../../models/response/base-response';
import { PageResponse } from '../../models/response/page-response';
import { ProductResponse } from '../../models/response/Product/ProductResponse';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ProductRequest } from '../../models/request/Product/ProductRequest';

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  private apiUrl = `${environment.apiUrl}/products`;

  constructor(private http: HttpClient) { }

  getAllProducts(pageNumber: number, pageSize: number): Observable<BaseResponse<PageResponse<ProductResponse>>> {
    const params = new HttpParams()
      .set('pageNumber', String(pageNumber))
      .set('pageSize', String(pageSize));
    return this.http.get<BaseResponse<PageResponse<ProductResponse>>>(this.apiUrl, { params });
  }

  getProductDetails(id: number): Observable<BaseResponse<ProductResponse>> {
    return this.http.get<BaseResponse<ProductResponse>>(`${this.apiUrl}/${id}`);
  }

  deleteProduct(id: number): Observable<BaseResponse<any>> {
    return this.http.delete<BaseResponse<any>>(`${this.apiUrl}/${id}`);
  }

  countProducts(): Observable<number> {
    return this.http.get<BaseResponse<number>>(`${this.apiUrl}/count`)
      .pipe(map(res => res.data));
  }

  /**
   * Create product với imageUrl từ Upload API
   */
  createProduct(request: ProductRequest): Observable<BaseResponse<any>> {
    return this.http.post<BaseResponse<any>>(`${this.apiUrl}/create`, request);
  }

  updateProduct(id: number, request: ProductRequest): Observable<BaseResponse<any>> {
    return this.http.patch<BaseResponse<any>>(`${this.apiUrl}/${id}`, request);
  }

  exportProductReport(name?: string): Observable<Blob> {
    let params = new HttpParams();
    if (name && name.trim()) {
      params = params.set('name', name.trim());
    }
    
    return this.http.get(`${environment.apiUrl}/reports/products`, {
      params: params,
      responseType: 'blob'
    });
  }

  uploadImage(id: number, file: File): Observable<BaseResponse<any>> {
    const form = new FormData();
    form.append('file', file);

    const headers = new HttpHeaders({
      Authorization: `Bearer ${localStorage.getItem('access_token') || ''}`
    });

    return this.http.post<BaseResponse<any>>(`${this.apiUrl}/${id}/image`, form, { headers });
  }

  searchByName(name: String): Observable<BaseResponse<ProductResponse[]>> {
    const params = new HttpParams().set('name', name.toString());
    return this.http.post<BaseResponse<ProductResponse[]>>(`${this.apiUrl}/search`, null, { params });
  }

  /**
   * Search products by category name with pagination
   */
  searchByCategory(categoryName: string, pageNumber: number = 0, pageSize: number = 12): Observable<BaseResponse<PageResponse<ProductResponse>>> {
    const params = new HttpParams()
      .set('name', categoryName)
      .set('pageNumber', pageNumber.toString())
      .set('pageSize', pageSize.toString());
    return this.http.post<BaseResponse<PageResponse<ProductResponse>>>(`${this.apiUrl}/search-category`, null, { params });
  }

  countByCategoryId(categoryId: number): Observable<number> {
    const params = new HttpParams().set('categoryId', categoryId.toString());
    return this.http.get<BaseResponse<number>>(`${this.apiUrl}/count-by-category`, { params })
      .pipe(map(res => res.data));
  }
}
