import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UserService } from '../../../core/services/users/user.service';
import { AuthService } from '../../../core/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { UserDetailsResponse } from '../../../core/models/response/User/UserDetailsRespomse';
import { UpdateUserRequest } from '../../../core/models/request/Users/UpdateUserRequest';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  user: UserDetailsResponse | null = null;
  isLoading = false;
  avatarUrl: string | null = null;
  
  // Edit profile modal
  showEditModal = false;
  editProfileForm!: FormGroup;
  isUpdating = false;
  
  // Avatar upload
  selectedAvatarFile: File | null = null;
  previewAvatarUrl: string | null = null;
  isUploadingAvatar = false;

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private toastService: ToastService,
    private fb: FormBuilder
  ) {
    this.initEditForm();
  }

  ngOnInit(): void {
    this.loadUserProfile();
  }

  initEditForm(): void {
    this.editProfileForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName: ['', [Validators.required, Validators.minLength(2)]]
    });
  }

  loadUserProfile(): void {
    this.isLoading = true;
    this.userService.getCurrentUser().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.user = response.data;
          this.avatarUrl = this.getAvatarUrl(this.user.avatarUrl);
          this.previewAvatarUrl = this.avatarUrl;
        }
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading user profile:', error);
        this.toastService.error('Không thể tải thông tin người dùng');
        this.isLoading = false;
      }
    });
  }

  // ===== EDIT PROFILE =====
  openEditModal(): void {
    if (this.user) {
      this.editProfileForm.patchValue({
        firstName: this.user.firstName || '',
        lastName: this.user.lastName || ''
      });
      this.showEditModal = true;
    }
  }

  closeEditModal(): void {
    this.showEditModal = false;
    this.editProfileForm.reset();
  }

  onSubmitEditProfile(): void {
    if (this.editProfileForm.valid && this.user) {
      this.isUpdating = true;
      
      const request: UpdateUserRequest = {
        firstName: this.editProfileForm.value.firstName,
        lastName: this.editProfileForm.value.lastName
      };
      
      this.userService.updateUser(this.user.id, request).subscribe({
        next: (response) => {
          if (response.success) {
            this.toastService.success('Cập nhật thông tin thành công');
            this.closeEditModal();
            this.loadUserProfile();
          } else {
            this.toastService.error(response.message || 'Cập nhật thất bại');
          }
          this.isUpdating = false;
        },
        error: (error) => {
          console.error('Error updating profile:', error);
          this.toastService.error('Có lỗi xảy ra khi cập nhật thông tin');
          this.isUpdating = false;
        }
      });
    } else {
      this.markFormGroupTouched(this.editProfileForm);
      this.toastService.error('Vui lòng điền đầy đủ thông tin hợp lệ');
    }
  }

  // ===== AVATAR UPLOAD =====
  onAvatarFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      // Validate file type
      const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif'];
      if (!validTypes.includes(file.type)) {
        this.toastService.error('Vui lòng chọn file ảnh (JPG, PNG, GIF)');
        return;
      }
      
      // Validate file size (max 5MB)
      if (file.size > 5 * 1024 * 1024) {
        this.toastService.error('Kích thước ảnh không được vượt quá 5MB');
        return;
      }
      
      this.selectedAvatarFile = file;
      
      // Preview
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.previewAvatarUrl = e.target.result;
      };
      reader.readAsDataURL(file);
      
      // Auto upload
      this.uploadAvatar();
    }
  }

  uploadAvatar(): void {
    if (this.selectedAvatarFile && this.user) {
      this.isUploadingAvatar = true;
      
      this.userService.uploadAvatar(this.user.id, this.selectedAvatarFile).subscribe({
        next: (response) => {
          if (response.success) {
            this.toastService.success('Cập nhật ảnh đại diện thành công');
            this.loadUserProfile();
          } else {
            this.toastService.error(response.message || 'Cập nhật ảnh thất bại');
            this.previewAvatarUrl = this.avatarUrl; // Reset preview
          }
          this.isUploadingAvatar = false;
          this.selectedAvatarFile = null;
        },
        error: (error) => {
          console.error('Error uploading avatar:', error);
          this.toastService.error('Có lỗi xảy ra khi tải ảnh lên');
          this.previewAvatarUrl = this.avatarUrl; // Reset preview
          this.isUploadingAvatar = false;
          this.selectedAvatarFile = null;
        }
      });
    }
  }

  deleteAvatar(): void {
    if (confirm('Bạn có chắc chắn muốn xóa ảnh đại diện?')) {
      this.toastService.info('Chức năng xóa ảnh đang được phát triển');
    }
  }

  // ===== HELPER METHODS =====

  getAvatarUrl(avatarUrl?: string): string | null {
    if (!avatarUrl) {
      return null;
    }
    return `${environment.assetBase}${avatarUrl.startsWith('/') ? '' : '/'}${avatarUrl}`;
  }

  getStatusBadgeClass(status: string): string {
    switch (status?.toLowerCase()) {
      case 'active':
        return 'status-active';
      case 'inactive':
        return 'status-inactive';
      case 'blocked':
        return 'status-blocked';
      default:
        return 'status-pending';
    }
  }

  getStatusText(status: string): string {
    switch (status?.toLowerCase()) {
      case 'active':
        return 'Đang hoạt động';
      case 'inactive':
        return 'Không hoạt động';
      case 'blocked':
        return 'Bị khóa';
      default:
        return 'Chờ xác thực';
    }
  }

  isFieldInvalid(form: FormGroup, fieldName: string): boolean {
    const field = form.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  getFieldError(form: FormGroup, fieldName: string): string {
    const field = form.get(fieldName);
    if (field && field.errors && (field.dirty || field.touched)) {
      if (field.errors['required']) {
        return 'Trường này là bắt buộc';
      }
      if (field.errors['minlength']) {
        return `Tối thiểu ${field.errors['minlength'].requiredLength} ký tự`;
      }
    }
    return '';
  }

  markFormGroupTouched(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      control?.markAsTouched();
    });
  }
}