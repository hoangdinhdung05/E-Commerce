import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClientService } from '../base/http-client.service';
import { ResourceService } from '../base/resource.service';
import { BaseResponse } from '../../models/response/base-response';
import { UserResponse } from '../../models/response/User/user-response';
import { AdminCreateUserRequest } from '../../models/request/Users/AdminCreateUserRequest';
import { UpdateUserRequest } from '../../models/request/Users/UpdateUserRequest';
import { UserDetailsResponse } from '../../models/response/User/UserDetailsRespomse';
import { ChangePasswordRequest } from '../../models/request/Users/ChangePasswordRequest';

/**
 * User service extending ResourceService for CRUD operations
 */
@Injectable({
  providedIn: 'root'
})
export class UserService extends ResourceService<
  UserResponse,
  AdminCreateUserRequest,
  UpdateUserRequest
> {
  constructor(http: HttpClientService) {
    super(http, '/users');
  }

  /**
   * Get current authenticated user details
   * @returns Observable of current user
   */
  getCurrentUser(): Observable<BaseResponse<UserDetailsResponse>> {
    return this.http.get<UserDetailsResponse>(`${this.baseUrl}/current`);
  }

  /**
   * Get user details by ID (returns UserDetailsResponse instead of UserResponse)
   * @param id - User identifier
   * @returns Observable of detailed user
   */
  getUserDetails(id: number): Observable<BaseResponse<UserDetailsResponse>> {
    return this.getDetails(id) as Observable<BaseResponse<UserDetailsResponse>>;
  }

  /**
   * Change user password
   * @param request - Password change request
   * @returns Observable of change result
   */
  changePassword(request: ChangePasswordRequest): Observable<BaseResponse<any>> {
    return this.http.post<any>(`${this.baseUrl}/change-password`, request);
  }

  /**
   * Upload user avatar
   * @param id - User identifier
   * @param file - Avatar image file
   * @returns Observable of upload result
   */
  uploadAvatar(id: number, file: File): Observable<BaseResponse<any>> {
    return this.uploadFile(id, file, 'avatar');
  }

  /**
   * Export user report
   * @param username - Optional username filter
   * @returns Observable of Blob (PDF)
   */
  exportUserReport(username?: string): Observable<Blob> {
    const params = username ? { username } : undefined;
    return this.exportReport('/reports/users', params);
  }
}
