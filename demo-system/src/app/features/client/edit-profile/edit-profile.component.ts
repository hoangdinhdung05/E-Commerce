import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UserService } from '../../../core/services/users/user.service';
import { AuthService } from '../../../core/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { UserDetailsResponse } from '../../../core/models/response/User/UserDetailsRespomse';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-edit-profile',
  templateUrl: './edit-profile.component.html',
  styleUrls: ['./edit-profile.component.css']
})
export class EditProfileComponent implements OnInit {
  profileForm!: FormGroup;
  user: UserDetailsResponse | null = null;
  isLoading = false;
  isSaving = false;
  avatarUrl: string | null = null;
  selectedFile: File | null = null;
  previewUrl: string | null = null;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private authService: AuthService,
    private toastService: ToastService
  ) {
    this.initForm();
  }

  ngOnInit(): void {
    this.loadUserProfile();
  }

  initForm(): void {
    this.profileForm = this.fb.group({
      username: [{ value: '', disabled: true }],
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.pattern(/^[0-9]{10,11}$/)]],
      address: [''],
      dateOfBirth: [''],
      gender: [''],
      bio: ['', [Validators.maxLength(500)]]
    });
  }

  loadUserProfile(): void {
    this.isLoading = true;
    this.userService.getCurrentUser().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.user = response.data;
          this.avatarUrl = this.getAvatarUrl(this.user.avatarUrl);
          this.populateForm();
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

  populateForm(): void {
    if (this.user) {
      this.profileForm.patchValue({
        username: this.user.username,
        firstName: this.user.firstName || '',
        lastName: this.user.lastName || '',
        email: this.user.email || '',
        phone: '', // Add when available from API
        address: '', // Add when available from API
        dateOfBirth: '', // Add when available from API
        gender: '', // Add when available from API
        bio: '' // Add when available from API
      });
    }
  }

  getAvatarUrl(avatarUrl?: string): string | null {
    if (!avatarUrl) {
      return null;
    }
    return `${environment.assetBase}${avatarUrl.startsWith('/') ? '' : '/'}${avatarUrl}`;
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const file = input.files[0];
      
      // Validate file type
      if (!file.type.startsWith('image/')) {
        this.toastService.error('Vui lòng chọn file hình ảnh');
        return;
      }
      
      // Validate file size (max 5MB)
      if (file.size > 5 * 1024 * 1024) {
        this.toastService.error('Kích thước file không được vượt quá 5MB');
        return;
      }
      
      this.selectedFile = file;
      
      // Create preview
      const reader = new FileReader();
      reader.onload = (e) => {
        this.previewUrl = e.target?.result as string;
      };
      reader.readAsDataURL(file);
    }
  }

  removeAvatar(): void {
    this.selectedFile = null;
    this.previewUrl = null;
    this.avatarUrl = null;
  }

  onSubmit(): void {
    if (this.profileForm.valid) {
      this.isSaving = true;
      
      const formData = this.profileForm.value;
      console.log('Saving profile:', formData);
      
      // Mock API call - Replace with actual API
      setTimeout(() => {
        this.toastService.success('Cập nhật thông tin thành công');
        this.isSaving = false;
        
        // Upload avatar if selected
        if (this.selectedFile) {
          this.uploadAvatar();
        }
      }, 2000);
    } else {
      this.markFormGroupTouched();
      this.toastService.error('Vui lòng điền đầy đủ thông tin hợp lệ');
    }
  }

  uploadAvatar(): void {
    if (this.selectedFile) {
      console.log('Uploading avatar:', this.selectedFile);
      
      // Mock upload - Replace with actual API
      setTimeout(() => {
        this.avatarUrl = this.previewUrl;
        this.selectedFile = null;
        this.previewUrl = null;
        this.toastService.success('Cập nhật ảnh đại diện thành công');
      }, 1500);
    }
  }

  markFormGroupTouched(): void {
    Object.keys(this.profileForm.controls).forEach(key => {
      const control = this.profileForm.get(key);
      control?.markAsTouched();
    });
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.profileForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  getFieldError(fieldName: string): string {
    const field = this.profileForm.get(fieldName);
    if (field && field.errors && (field.dirty || field.touched)) {
      if (field.errors['required']) {
        return 'Trường này là bắt buộc';
      }
      if (field.errors['email']) {
        return 'Email không hợp lệ';
      }
      if (field.errors['minlength']) {
        return `Tối thiểu ${field.errors['minlength'].requiredLength} ký tự`;
      }
      if (field.errors['maxlength']) {
        return `Tối đa ${field.errors['maxlength'].requiredLength} ký tự`;
      }
      if (field.errors['pattern']) {
        return 'Định dạng không hợp lệ';
      }
    }
    return '';
  }

  resetForm(): void {
    this.profileForm.reset();
    this.selectedFile = null;
    this.previewUrl = null;
    this.populateForm();
  }

  cancelEdit(): void {
    if (this.profileForm.dirty || this.selectedFile) {
      if (confirm('Bạn có thay đổi chưa được lưu. Bạn có chắc chắn muốn hủy?')) {
        this.resetForm();
      }
    }
  }
}