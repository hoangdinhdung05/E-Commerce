import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { BaseResponse } from '../../models/response/base-response';
import { PageResponse } from '../../models/response/page-response';

/**
 * Centralized HTTP client service with error handling and common request patterns
 * Wraps Angular HttpClient to provide consistent response types and error handling
 */
@Injectable({
  providedIn: 'root'
})
export class HttpClientService {
  constructor(private http: HttpClient) {}

  /**
   * Perform GET request
   * @param url - API endpoint URL
   * @param params - Optional HTTP parameters
   * @returns Observable of BaseResponse<T>
   */
  get<T>(url: string, params?: HttpParams): Observable<BaseResponse<T>> {
    return this.http.get<BaseResponse<T>>(url, { params })
      .pipe(catchError(this.handleError));
  }

  /**
   * Perform GET request with pagination
   * @param url - API endpoint URL
   * @param page - Zero-based page number
   * @param size - Items per page
   * @param additionalParams - Optional additional HTTP parameters
   * @returns Observable of paginated response
   */
  getPaginated<T>(
    url: string,
    page: number,
    size: number,
    additionalParams?: HttpParams
  ): Observable<BaseResponse<PageResponse<T>>> {
    let params = new HttpParams()
      .set('pageNumber', String(page))
      .set('pageSize', String(size));

    if (additionalParams) {
      additionalParams.keys().forEach(key => {
        const value = additionalParams.get(key);
        if (value) {
          params = params.set(key, value);
        }
      });
    }

    return this.http.get<BaseResponse<PageResponse<T>>>(url, { params })
      .pipe(catchError(this.handleError));
  }

  /**
   * Perform POST request
   * @param url - API endpoint URL
   * @param body - Request payload
   * @param params - Optional HTTP parameters
   * @returns Observable of BaseResponse<T>
   */
  post<T>(url: string, body: any, params?: HttpParams): Observable<BaseResponse<T>> {
    return this.http.post<BaseResponse<T>>(url, body, { params })
      .pipe(catchError(this.handleError));
  }

  /**
   * Perform PATCH request
   * @param url - API endpoint URL
   * @param body - Request payload (partial update)
   * @returns Observable of BaseResponse<T>
   */
  patch<T>(url: string, body: any): Observable<BaseResponse<T>> {
    return this.http.patch<BaseResponse<T>>(url, body)
      .pipe(catchError(this.handleError));
  }

  /**
   * Perform PUT request
   * @param url - API endpoint URL
   * @param body - Request payload (full update)
   * @returns Observable of BaseResponse<T>
   */
  put<T>(url: string, body: any): Observable<BaseResponse<T>> {
    return this.http.put<BaseResponse<T>>(url, body)
      .pipe(catchError(this.handleError));
  }

  /**
   * Perform DELETE request
   * @param url - API endpoint URL
   * @returns Observable of BaseResponse<void>
   */
  delete<T = void>(url: string): Observable<BaseResponse<T>> {
    return this.http.delete<BaseResponse<T>>(url)
      .pipe(catchError(this.handleError));
  }

  /**
   * Download file as Blob (for reports/exports)
   * @param url - API endpoint URL
   * @param params - Optional HTTP parameters
   * @returns Observable of Blob
   */
  downloadBlob(url: string, params?: HttpParams): Observable<Blob> {
    return this.http.get(url, { params, responseType: 'blob' })
      .pipe(catchError(this.handleError));
  }

  /**
   * Upload file with FormData
   * @param url - API endpoint URL
   * @param formData - FormData containing file(s)
   * @returns Observable of BaseResponse<T>
   */
  uploadFile<T>(url: string, formData: FormData): Observable<BaseResponse<T>> {
    // Note: Don't set Content-Type header manually, browser sets it with boundary
    return this.http.post<BaseResponse<T>>(url, formData)
      .pipe(catchError(this.handleError));
  }

  /**
   * Centralized error handler
   * @param error - HttpErrorResponse
   * @returns Observable that throws formatted error
   */
  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'An unknown error occurred';

    if (error.error instanceof ErrorEvent) {
      // Client-side or network error
      errorMessage = `Client Error: ${error.error.message}`;
    } else {
      // Backend returned unsuccessful response code
      errorMessage = error.error?.message || 
                     `Server Error: ${error.status} - ${error.statusText}`;
    }

    console.error('HTTP Error:', errorMessage, error);
    return throwError(() => new Error(errorMessage));
  }
}
