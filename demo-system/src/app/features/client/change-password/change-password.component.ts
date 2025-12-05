import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/auth.service';
import { UserService } from '../../../core/services/users/user.service';
import { ToastService } from '../../../core/services/toast.service';
import { ChangePasswordRequest } from '../../../core/models/request/Users/ChangePasswordRequest';

@Component({
  selector: 'app-change-password',
  templateUrl: './change-password.component.html',
  styleUrls: ['./change-password.component.css']
})
export class ChangePasswordComponent implements OnInit {
  changePasswordForm!: FormGroup;
  isLoading = false;
  showCurrentPassword = false;
  showNewPassword = false;
  showConfirmPassword = false;

  passwordRequirements = {
    minLength: false,
    hasUppercase: false,
    hasLowercase: false,
    hasNumber: false,
    hasSpecial: false
  };

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private userService: UserService,
    private toastService: ToastService,
    private router: Router
  ) {
    this.initForm();
  }

  ngOnInit(): void {
  }

  initForm(): void {
    this.changePasswordForm = this.fb.group({
      currentPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required, this.strongPasswordValidator]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordMatchValidator });

    // Watch new password changes to update requirements
    this.changePasswordForm.get('newPassword')?.valueChanges.subscribe(password => {
      this.updatePasswordRequirements(password || '');
    });
  }

  strongPasswordValidator(control: AbstractControl): { [key: string]: any } | null {
    const password = control.value;
    if (!password) return null;

    const minLength = password.length >= 8;
    const hasUppercase = /[A-Z]/.test(password);
    const hasLowercase = /[a-z]/.test(password);
    const hasNumber = /\d/.test(password);
    const hasSpecial = /[!@#$%^&*(),.?":{}|<>]/.test(password);

    const isValid = minLength && hasUppercase && hasLowercase && hasNumber && hasSpecial;
    
    return isValid ? null : { strongPassword: true };
  }

  passwordMatchValidator(group: AbstractControl): { [key: string]: any } | null {
    const newPassword = group.get('newPassword')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;
    
    if (newPassword && confirmPassword && newPassword !== confirmPassword) {
      return { passwordMismatch: true };
    }
    
    return null;
  }

  updatePasswordRequirements(password: string): void {
    this.passwordRequirements = {
      minLength: password.length >= 8,
      hasUppercase: /[A-Z]/.test(password),
      hasLowercase: /[a-z]/.test(password),
      hasNumber: /\d/.test(password),
      hasSpecial: /[!@#$%^&*(),.?":{}|<>]/.test(password)
    };
  }

  togglePasswordVisibility(field: string): void {
    switch (field) {
      case 'current':
        this.showCurrentPassword = !this.showCurrentPassword;
        break;
      case 'new':
        this.showNewPassword = !this.showNewPassword;
        break;
      case 'confirm':
        this.showConfirmPassword = !this.showConfirmPassword;
        break;
    }
  }

  onSubmit(): void {
    if (this.changePasswordForm.valid) {
      this.isLoading = true;
      
      const formData = this.changePasswordForm.value;
      
      const request: ChangePasswordRequest = {
        oldPassword: formData.currentPassword,
        newPassword: formData.newPassword,
        confirmPassword: formData.confirmPassword
      };
      
      this.userService.changePassword(request).subscribe({
        next: (response) => {
          if (response.success) {
            this.toastService.success('Đổi mật khẩu thành công! Vui lòng đăng nhập lại');
            this.changePasswordForm.reset();
            
            // Logout and redirect to login
            setTimeout(() => {
              localStorage.removeItem('access_token');
              localStorage.removeItem('refresh_token');
              this.router.navigate(['/auth/login']);
            }, 1500);
          } else {
            this.toastService.error(response.message || 'Đổi mật khẩu thất bại');
          }
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Error changing password:', error);
          const errorMessage = error.error?.message || 'Có lỗi xảy ra khi đổi mật khẩu';
          this.toastService.error(errorMessage);
          this.isLoading = false;
        }
      });
      
    } else {
      this.markFormGroupTouched();
      this.toastService.error('Vui lòng điền đầy đủ thông tin hợp lệ');
    }
  }

  markFormGroupTouched(): void {
    Object.keys(this.changePasswordForm.controls).forEach(key => {
      const control = this.changePasswordForm.get(key);
      control?.markAsTouched();
    });
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.changePasswordForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  getFieldError(fieldName: string): string {
    const field = this.changePasswordForm.get(fieldName);
    if (field && field.errors && (field.dirty || field.touched)) {
      if (field.errors['required']) {
        return 'Trường này là bắt buộc';
      }
      if (field.errors['strongPassword']) {
        return 'Mật khẩu không đủ mạnh';
      }
    }

    // Check form-level errors
    if (fieldName === 'confirmPassword' && this.changePasswordForm.errors?.['passwordMismatch']) {
      return 'Mật khẩu xác nhận không khớp';
    }

    return '';
  }

  getPasswordStrength(): string {
    const requirements = Object.values(this.passwordRequirements);
    const metCount = requirements.filter(req => req).length;
    
    if (metCount <= 2) return 'weak';
    if (metCount <= 4) return 'medium';
    return 'strong';
  }

  getPasswordStrengthText(): string {
    const strength = this.getPasswordStrength();
    switch (strength) {
      case 'weak': return 'Yếu';
      case 'medium': return 'Trung bình';
      case 'strong': return 'Mạnh';
      default: return '';
    }
  }

  getPasswordStrengthClass(): string {
    return `strength-${this.getPasswordStrength()}`;
  }
}