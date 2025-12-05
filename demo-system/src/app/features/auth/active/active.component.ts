import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from 'src/app/core/auth.service';
import { ToastService } from 'src/app/core/services/toast.service';
import { OtpService } from 'src/app/core/services/otp/otp.service';
import { ResendOtpRequest } from 'src/app/core/models/request/Otp/resend-otp-request';

@Component({
  selector: 'app-active',
  templateUrl: './active.component.html',
  styleUrls: ['./active.component.css']
})
export class ActiveComponent implements OnInit {

  activeForm!: FormGroup;
  email!: string;
  isResending = false;
  countdown = 0;
  countdownInterval: any;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private authService: AuthService,
    private otpService: OtpService,
    private router: Router,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    // Lấy email từ query param
    this.route.queryParams.subscribe(params => {
      this.email = params['email'];
    });

    // Form chỉ có trường OTP
    this.activeForm = this.fb.group({
      otp: ['', [Validators.required, Validators.minLength(4)]],
    });
  }

  ngOnDestroy(): void {
    if (this.countdownInterval) {
      clearInterval(this.countdownInterval);
    }
  }

  onSubmit(): void {
    if (this.activeForm.invalid) {
      this.toast.error('Vui lòng nhập mã OTP!');
      return;
    }

    const payload = {
      email: this.email,
      otp: this.activeForm.value.otp
    };

    this.authService.active(payload).subscribe({
      next: (res) => {
        if (res.success) {
          this.toast.success('Kích hoạt tài khoản thành công! Bạn có thể đăng nhập.');
          this.router.navigate(['/auth/login']);
        } else {
          this.toast.error(res.message || 'Kích hoạt thất bại!');
        }
      },
      error: (err) => {
        this.toast.error(err.error?.message || 'Mã OTP không hợp lệ hoặc đã hết hạn!');
      }
    });
  }

  onResendOtp(): void {
    if (this.isResending || this.countdown > 0) {
      return;
    }

    if (!this.email) {
      this.toast.error('Không tìm thấy email!');
      return;
    }

    this.isResending = true;

    const request: ResendOtpRequest = {
      email: this.email,
      type: 'VERIFY_EMAIL'
    };

    this.otpService.resendOtp(request).subscribe({
      next: (res) => {
        this.isResending = false;
        if (res.success) {
          this.toast.success('Mã OTP mới đã được gửi đến email của bạn!');
          this.startCountdown(60); // Đếm ngược 60 giây
        } else {
          this.toast.error(res.message || 'Gửi lại OTP thất bại!');
        }
      },
      error: (err) => {
        this.isResending = false;
        this.toast.error(err.error?.message || 'Có lỗi xảy ra khi gửi lại OTP!');
      }
    });
  }

  private startCountdown(seconds: number): void {
    this.countdown = seconds;
    
    if (this.countdownInterval) {
      clearInterval(this.countdownInterval);
    }

    this.countdownInterval = setInterval(() => {
      this.countdown--;
      if (this.countdown <= 0) {
        clearInterval(this.countdownInterval);
      }
    }, 1000);
  }

  get canResend(): boolean {
    return !this.isResending && this.countdown === 0;
  }

  get resendButtonText(): string {
    if (this.isResending) {
      return 'Đang gửi...';
    }
    if (this.countdown > 0) {
      return `Gửi lại (${this.countdown}s)`;
    }
    return 'Gửi lại mã OTP';
  }
}
