import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/core/auth.service';
import { CartService } from 'src/app/core/services/cart/cart.service';
import { LoginRequest } from 'src/app/core/models/request/login-request';
import { ToastrService } from 'ngx-toastr';
 

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  loginForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private cartService: CartService,
    private router: Router,
    private toastr: ToastrService
  ) {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  onSubmit() {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      this.toastr.error('Vui lòng nhập đầy đủ thông tin!');
      return;
    }

    const request: LoginRequest = this.loginForm.value;
    this.authService.login(request).subscribe({
      next: (response) => {
        const access_token = response.data.accessToken;
        const refresh_token = response.data.refreshToken;
        // AuthService.login() already saves tokens via tap; use helper to read roles
        this.toastr.success('Đăng nhập thành công!');

        // Refresh cart sau khi đăng nhập thành công
        this.cartService.refreshCart();

        const roles = this.authService.getRoles();
        const firstRole = roles.length ? roles[0] : null;

        console.log('ROLE:', roles);

        if (roles.includes('ROLE_ADMIN') || firstRole === 'ROLE_ADMIN') {
          this.router.navigate(['/admin']);
        } else if (roles.includes('ROLE_USER') || firstRole === 'ROLE_USER') {
          this.router.navigate(['/']); // client home
        } else {
          this.router.navigate(['/auth/login']);
        }
      },
      error: (err) => {

        if (err.error.message) {
          this.toastr.error(err.error.message);
          return;
        } else {
          this.toastr.error('Tên đăng nhập hoặc mật khẩu không đúng!');
        }

      }
    });
  }

}
