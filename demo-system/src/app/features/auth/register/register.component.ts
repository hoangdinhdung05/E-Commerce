import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/core/auth.service';
import { RegisterRequest } from 'src/app/core/models/request/register-request';
import { ToastService } from 'src/app/core/services/toast.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {

  registerForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private authService: AuthService,
    private toast: ToastService
  ) {
    this.registerForm = this.fb.group({
      username: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  onSubmit() {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      this.toast.error('Vui lòng điền đầy đủ thông tin!');
      return;
    }

    const request: RegisterRequest = this.registerForm.value;

    this.authService.register(request).subscribe({
      next: (res) => {
        if (res.success) {
          this.toast.success('Đăng ký thành công! Vui lòng kiểm tra email để nhận mã OTP.');

          // 👉 Truyền email sang trang active
          const email = this.registerForm.value.email;
          this.router.navigate(['/auth/active'], { queryParams: { email } });
        } else {
          this.toast.error(res.message || 'Đăng ký thất bại!');
        }
      },
      error: (err) => {
        if (err.error && err.error.errors) {
          // Lấy object lỗi từ backend
          const errors = err.error.errors;

          // Nếu là object { password: "...", username: "..." }
          const messages = Object.values(errors).join('\n');
          this.toast.error(messages);

        } else if (err.error && err.error.message) {
          // Nếu backend chỉ trả message
          this.toast.error(err.error.message);

        } else {
          this.toast.error('Có lỗi xảy ra!');
        }

        console.error('Register error:', err);
      }
    });
  }
}
