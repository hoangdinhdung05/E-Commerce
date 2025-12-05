import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { HttpClientService } from './http-client.service';
import { BaseResponse } from '../../models/response/base-response';
import { PageResponse } from '../../models/response/page-response';
import { environment } from 'src/environments/environment';

/**
 * Generic base service for RESTful CRUD operations
 * Reduces code duplication by providing common HTTP operations
 * 
 * @template T - Response DTO type
 * @template CreateDTO - Create request DTO type
 * @template UpdateDTO - Update request DTO type
 * 
 * @example
 * ```typescript
 * @Injectable({ providedIn: 'root' })
 * export class ProductService extends ResourceService<
 *   ProductResponse,
 *   ProductCreateRequest,
 *   ProductRequest
 * > {
 *   constructor(http: HttpClientService) {
 *     super(http, '/products');
 *   }
 *   
 *   // Add domain-specific methods
 *   searchByCategory(category: string) {...}
 * }
 * ```
 */
@Injectable()
export abstract class ResourceService<T, CreateDTO = any, UpdateDTO = any> {
  protected readonly baseUrl: string;

  protected constructor(
    protected http: HttpClientService,
    protected resourcePath: string
  ) {
    this.baseUrl = `${environment.apiUrl}${resourcePath}`;
  }

  /**
   * Get all resources with pagination
   * @param page - Zero-based page number
   * @param size - Items per page
   * @returns Observable of paginated response
   */
  getAll(page: number, size: number): Observable<BaseResponse<PageResponse<T>>> {
    return this.http.getPaginated<T>(this.baseUrl, page, size);
  }

  /**
   * Get single resource by ID
   * @param id - Resource identifier
   * @returns Observable of single resource
   */
  getById(id: number): Observable<BaseResponse<T>> {
    return this.http.get<T>(`${this.baseUrl}/${id}`);
  }

  /**
   * Get resource details (alternative endpoint pattern)
   * Some APIs use /details/:id instead of /:id
   * @param id - Resource identifier
   * @returns Observable of single resource
   */
  getDetails(id: number): Observable<BaseResponse<T>> {
    return this.http.get<T>(`${this.baseUrl}/details/${id}`);
  }

  /**
   * Create new resource
   * @param dto - Create request DTO
   * @returns Observable of created resource
   */
  create(dto: CreateDTO): Observable<BaseResponse<T>> {
    return this.http.post<T>(this.baseUrl, dto);
  }

  /**
   * Update existing resource (PATCH)
   * @param id - Resource identifier
   * @param dto - Update request DTO (partial)
   * @returns Observable of updated resource
   */
  update(id: number, dto: UpdateDTO): Observable<BaseResponse<T>> {
    return this.http.patch<T>(`${this.baseUrl}/${id}`, dto);
  }

  /**
   * Delete resource
   * @param id - Resource identifier
   * @returns Observable of delete result
   */
  delete(id: number): Observable<BaseResponse<void>> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  /**
   * Get resource count
   * @returns Observable of count number
   */
  count(): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/count`)
      .pipe(map(response => response.data));
  }

  /**
   * Export resource report as Blob
   * @param filename - Optional filename parameter
   * @returns Observable of Blob (PDF, Excel, etc.)
   */
  exportReport(reportPath: string, params?: any): Observable<Blob> {
    const url = `${environment.apiUrl}${reportPath}`;
    const httpParams = params ? this.toHttpParams(params) : undefined;
    return this.http.downloadBlob(url, httpParams);
  }

  /**
   * Upload file for resource
   * @param id - Resource identifier
   * @param file - File to upload
   * @param fieldName - Form field name (default: 'file')
   * @returns Observable of upload result
   */
  uploadFile(id: number, file: File, fieldName: string = 'file'): Observable<BaseResponse<any>> {
    const formData = new FormData();
    formData.append(fieldName, file);
    return this.http.uploadFile(`${this.baseUrl}/${id}/${fieldName}`, formData);
  }

  /**
   * Helper: Convert object to HttpParams
   * @param params - Parameter object
   * @returns HttpParams instance
   */
  protected toHttpParams(params: any): HttpParams {
    let httpParams = new HttpParams();
    Object.keys(params).forEach(key => {
      const value = params[key];
      if (value !== null && value !== undefined && value !== '') {
        httpParams = httpParams.set(key, String(value));
      }
    });
    return httpParams;
  }
}
