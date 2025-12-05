import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { BaseResponse } from '../../models/response/base-response';
import { PageResponse } from '../../models/response/page-response';
import { UserResponse } from '../../models/response/User/user-response';
import { AdminCreateUserRequest } from '../../models/request/Users/AdminCreateUserRequest';
import { UpdateUserRequest } from '../../models/request/Users/UpdateUserRequest';
import { UserDetailsResponse } from '../../models/response/User/UserDetailsRespomse';
import { map } from 'rxjs/operators';
import { ChangePasswordRequest } from '../../models/request/Users/ChangePasswordRequest';


@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = `${environment.apiUrl}/users`;
  
  constructor(private http: HttpClient) { }

  getAllUsers(pageNumber: number, pageSize: number): Observable<BaseResponse<PageResponse<UserResponse>>> {
    const params = new HttpParams()
      .set('pageNumber', pageNumber)
      .set('pageSize', pageSize);
    
    return this.http.get<BaseResponse<PageResponse<UserResponse>>>(this.apiUrl, { params });
  }

  getUserDetails(id: number): Observable<BaseResponse<UserDetailsResponse>> {
    return this.http.get<BaseResponse<UserDetailsResponse>>(`${this.apiUrl}/details/${id}`);
  }

  getCurrentUser(): Observable<BaseResponse<UserDetailsResponse>> {
    return this.http.get<BaseResponse<UserDetailsResponse>>(`${this.apiUrl}/current`);
  }

  adminCreateUser(request: AdminCreateUserRequest): Observable<BaseResponse<any>> {
    return this.http.post<BaseResponse<any>>(`${this.apiUrl}`, request);
  }  

  updateUser(id: number, request: UpdateUserRequest): Observable<BaseResponse<any>> {
    return this.http.patch<BaseResponse<any>>(`${this.apiUrl}/${id}`, request);
  }

  deleteUser(id: number): Observable<BaseResponse<any>> {
    return this.http.delete<BaseResponse<any>>(`${this.apiUrl}/${id}`);
  }

  exportUserReport(username?: string): Observable<Blob> {
    let params = new HttpParams();
    if (username && username.trim()) {
      params = params.set('username', username.trim());
    }
    
    return this.http.get(`${environment.apiUrl}/reports/users`, {
      params: params,
      responseType: 'blob'
    });
  }

  countUser(): Observable<number> {
    return this.http.get<BaseResponse<number>>(`${this.apiUrl}/count`)
      .pipe(map(res => res.data));
  }

  changePassword(request: ChangePasswordRequest): Observable<BaseResponse<any>> {
    const headers = new HttpHeaders({
      Authorization: `Bearer ${localStorage.getItem('access_token') || ''}`
    });
    return this.http.post<BaseResponse<any>>(`${this.apiUrl}/change-password`, request, { headers });
  }

  /**
   * Upload avatar file for a user. Accepts a File object and posts as multipart/form-data.
   */
  uploadAvatar(id: number, file: File): Observable<BaseResponse<any>> {
    const form = new FormData();
    form.append('file', file);

    const headers = new HttpHeaders({
      Authorization: `Bearer ${localStorage.getItem('access_token') || ''}`
    });

    return this.http.post<BaseResponse<any>>(`${this.apiUrl}/${id}/avatar`, form, { headers });
  }
}
