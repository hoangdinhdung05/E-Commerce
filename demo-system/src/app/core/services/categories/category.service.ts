import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClientService } from '../base/http-client.service';
import { ResourceService } from '../base/resource.service';
import { BaseResponse } from '../../models/response/base-response';
import { CategoryResponse } from '../../models/response/Category/CategoryResponse';
import { CategoryRequest } from '../../models/request/Category/CategoryRequest';

/**
 * Category service extending ResourceService for CRUD operations
 */
@Injectable({
  providedIn: 'root'
})
export class CategoryService extends ResourceService<
  CategoryResponse,
  CategoryRequest,
  CategoryRequest
> {
  constructor(http: HttpClientService) {
    super(http, '/categories');
  }

  /**
   * Create category with custom endpoint
   * @param request - Category creation request
   * @returns Observable of creation result
   */
  adminCreateCategory(request: CategoryRequest): Observable<BaseResponse<any>> {
    return this.http.post<any>(`${this.baseUrl}/add`, request);
  }

  /**
   * Export categories report
   * @returns Observable of Blob (PDF)
   */
  exportCategoryReport(): Observable<Blob> {
    return this.exportReport('/reports/categories');
  }
}
