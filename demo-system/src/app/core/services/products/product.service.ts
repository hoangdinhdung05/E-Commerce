import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { HttpParams } from '@angular/common/http';
import { HttpClientService } from '../base/http-client.service';
import { ResourceService } from '../base/resource.service';
import { BaseResponse } from '../../models/response/base-response';
import { PageResponse } from '../../models/response/page-response';
import { ProductResponse } from '../../models/response/Product/ProductResponse';
import { ProductRequest } from '../../models/request/Product/ProductRequest';

/**
 * Product service with CRUD operations and domain-specific methods
 * Extends ResourceService to inherit common REST operations
 */
@Injectable({
  providedIn: 'root'
})
export class ProductService extends ResourceService<
  ProductResponse,
  ProductRequest,
  ProductRequest
> {
  constructor(http: HttpClientService) {
    super(http, '/products');
  }

  /**
   * Search products by name
   * @param name - Product name to search
   * @returns Observable of matching products
   */
  searchByName(name: string): Observable<BaseResponse<ProductResponse[]>> {
    const params = new HttpParams().set('name', name);
    return this.http.post<ProductResponse[]>(`${this.baseUrl}/search`, null, params);
  }

  /**
   * Search products by category name with pagination
   * @param categoryName - Category name
   * @param page - Page number (default: 0)
   * @param size - Page size (default: 12)
   * @returns Observable of paginated products
   */
  searchByCategory(
    categoryName: string,
    page: number = 0,
    size: number = 12
  ): Observable<BaseResponse<PageResponse<ProductResponse>>> {
    const params = this.toHttpParams({ name: categoryName, pageNumber: page, pageSize: size });
    return this.http.post<PageResponse<ProductResponse>>(`${this.baseUrl}/search-category`, null, params);
  }

  /**
   * Count products by category ID
   * @param categoryId - Category identifier
   * @returns Observable of product count
   */
  countByCategoryId(categoryId: number): Observable<number> {
    const params = this.toHttpParams({ categoryId });
    return this.http.get<number>(`${this.baseUrl}/count-by-category`, params)
      .pipe(map(res => res.data));
  }

  /**
   * Upload product image
   * @param id - Product identifier
   * @param file - Image file
   * @returns Observable of upload result
   */
  uploadProductImage(id: number, file: File): Observable<BaseResponse<any>> {
    return this.uploadFile(id, file, 'image');
  }

  /**
   * Export product report
   * @param name - Optional product name filter
   * @returns Observable of Blob (PDF)
   */
  exportProductReport(name?: string): Observable<Blob> {
    const params = name ? { name } : undefined;
    return this.exportReport('/reports/products', params);
  }
}
