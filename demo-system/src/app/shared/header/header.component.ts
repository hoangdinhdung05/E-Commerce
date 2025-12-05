import { Component, OnInit, HostListener } from '@angular/core';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from 'src/app/core/auth.service';
import { UserDetailsResponse } from 'src/app/core/models/response/User/UserDetailsRespomse';
import { UserService } from 'src/app/core/services/users/user.service';
import { UpdateUserRequest } from 'src/app/core/models/request/Users/UpdateUserRequest';
import { ChangePasswordRequest } from 'src/app/core/models/request/Users/ChangePasswordRequest';
import { ViewChild, ElementRef } from '@angular/core';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {
  user?: UserDetailsResponse;
  userInitial: string = '';
  isMenuOpen = false;

  userId: number = 0;
  @ViewChild('avatarInput') avatarInput?: ElementRef<HTMLInputElement>;

  showProfileModal = false;
  showEditModal = false;
  showChangePasswordModal = false;

  oldPassword: string = '';
  newPassword: string = '';
  confirmPassword: string = '';

  avatarUrl: string = '';


  constructor(
    private router: Router,
    private authService: AuthService,
    private userService: UserService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    const token = localStorage.getItem('access_token');
    if (!token) {
      this.router.navigate(['/auth/login']);
      return;
    }

    const userId = this.decodeUserIdFromToken(token);
    this.userId = userId;
    this.loadUserDetails(userId);
  }

  private decodeUserIdFromToken(token: string): number {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.userId; // hoặc field thực tế trong payload token
    } catch (err) {
      console.error('Không thể decode token', err);
      return 0;
    }
  }

  private loadUserDetails(userId: number) {
    if (!userId) return;
    this.userService.getUserDetails(userId).subscribe({
      next: (res) => {
        this.user = res.data;

        console.log('User details:', this.user);

        this.userInitial = this.getUserInitial(this.user.username);
        this.avatarUrl = this.buildAvatarSrc(this.user.avatarUrl);
      },
      error: (err) => console.error('Lỗi khi lấy thông tin user:', err)
    });
  }

  private buildAvatarSrc(avatarUrl?: string | null): string {
    if (!avatarUrl) return '';
    // Nếu BE đã trả absolute (http/https), dùng luôn
    if (/^https?:\/\//i.test(avatarUrl)) return avatarUrl;
    // Nếu BE trả đường dẫn tương đối kiểu /avatars/xxx.jpg
    return `${environment.url}${avatarUrl.startsWith('/') ? '' : '/'}${avatarUrl}`;
  }

  private getUserInitial(username: string): string {
    return username ? username.charAt(0).toUpperCase() : '?';
  }

  toggleMenu(): void {
    this.isMenuOpen = !this.isMenuOpen;
  }

  @HostListener('document:click', ['$event'])
  closeMenu(event: Event): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.user-menu-wrapper')) {
      this.isMenuOpen = false;
    }
  }

  viewProfile(): void {
    this.showProfileModal = true;
  }

  editProfile(): void {
    this.showEditModal = true;
  }

  changePassword(): void {
    this.showChangePasswordModal = true;
  }

  closeModals(): void {
    this.showProfileModal = false;
    this.showEditModal = false;
    this.showChangePasswordModal = false;
  }

  logout() {
    this.authService.logout();
    this.toastr.success("Đăng xuất thành công");
    this.router.navigate(['/auth/login']);
  }

  updateProfile() {
    if (!this.user) return;
    const request: UpdateUserRequest = {
      firstName: this.user.firstName,
      lastName: this.user.lastName
    };
    this.userService.updateUser(this.user.id, request).subscribe({
      next: () => this.toastr.success('Cập nhật thành công'),
      error: () => this.toastr.error('Cập nhật thất bại')
    });
  }

  changePasswordTest(oldPassword: string, newPassword: string, confirmPassword: string) {
    if (!this.user) return;

    const request: ChangePasswordRequest = {
      oldPassword,
      newPassword,
      confirmPassword
    };

    this.userService.changePassword(request).subscribe({
      next: (res) => this.toastr.success('Đổi mật khẩu thành công'),
      error: (err) => this.toastr.error('Đổi mật khẩu thất bại')
    });
  }

  triggerAvatarSelect(): void {
    if (this.avatarInput) {
      this.avatarInput.nativeElement.click();
    }
  }

  onAvatarSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;
    const file = input.files[0];
    if (!file) return;

    this.userService.uploadAvatar(this.userId, file).subscribe({
      next: () => {
        this.toastr.success('Upload avatar thành công');
        // reload user details to get new avatar url
        if (this.userId) this.loadUserDetails(this.userId);
      },
      error: (err) => {
        console.error('Lỗi upload avatar', err);
        this.toastr.error('Upload avatar thất bại');
      }
    });
  }
}
