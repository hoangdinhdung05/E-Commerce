import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UserService } from 'src/app/core/services/users/user.service';
import { AdminCreateUserRequest } from 'src/app/core/models/request/Users/AdminCreateUserRequest';
import { ToastrService } from 'ngx-toastr';

declare var bootstrap: any;

@Component({
  selector: 'app-users',
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.css']
})
export class UsersComponent implements OnInit {
  @ViewChild('createUserModal') createUserModal!: ElementRef;

  users: any[] = [];
  filteredUsers: any[] = [];
  totalPages = 0;
  totalElements = 0;
  currentPage = 0;
  pageSize = 10;
  loading = false;

  createUserForm!: FormGroup;
  modalInstance: any;

  searchText = '';
  roleOptions: string[] = ['USER', 'ADMIN'];
  backendErrors: { [key: string]: string } = {};


  @ViewChild('editUserModal') editUserModal!: ElementRef;
  editUserForm!: FormGroup;
  selectedUser: any = null;
  editModalInstance: any;

  @ViewChild('detailsUserModal') detailsUserModal!: ElementRef;
  detailsModalInstance: any;
  selectedUserDetails: any = null;

  @ViewChild('deleteUserModal') deleteUserModal!: ElementRef;
  deleteModalInstance: any;
  userToDelete: any = null;

  constructor(private userService: UserService, 
    private fb: FormBuilder, 
    private toastr: ToastrService) {}

  ngOnInit(): void {
    this.loadUsers(this.currentPage > 0 && this.filteredUsers.length === 1 
      ? this.currentPage - 1 
      : this.currentPage);

    this.createUserForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(20)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8), Validators.pattern(/^(?=.*[A-Za-z])(?=.*\d).+$/)]],
      roles: [[]] // mảng chứa các role được chọn
    });

    this.editUserForm = this.fb.group({
      firstName: [''],
      lastName: [''],
      status: ['ACTIVE'],
      verifyEmail: [false],
      roles: [[]]
    });
  }

  onRoleChange(event: any) {
    const roles: string[] = this.createUserForm.get('roles')?.value || [];
    if (event.target.checked) {
      roles.push(event.target.value);
    } else {
      const index = roles.indexOf(event.target.value);
      if (index >= 0) roles.splice(index, 1);
    }
    this.createUserForm.get('roles')?.setValue(roles);
  }

  // === API load users ===
  loadUsers(page: number) {
    this.loading = true;
    this.userService.getAllUsers(page, this.pageSize).subscribe({
      next: res => {
        if (res.success) {
          this.users = res.data.content;
          this.filteredUsers = [...this.users]; // filter search
          this.currentPage = res.data.pageNumber;
          this.totalPages = res.data.totalPages;
          this.totalElements = res.data.totalElements;
        }
        this.loading = false;
      },
      error: err => {
        console.error('Error loading users', err);
        this.loading = false;
      }
    });
  }

  nextPage() { if (this.currentPage < this.totalPages - 1) this.loadUsers(this.currentPage + 1); }
  previousPage() { if (this.currentPage > 0) this.loadUsers(this.currentPage - 1); }
  goToPage(page: number) { this.loadUsers(page); }

  getPageNumbers(): number[] {
    const maxPagesToShow = 5;
    const pages: number[] = [];
    
    if (this.totalPages <= maxPagesToShow) {
      // Nếu tổng số trang <= 5, hiển thị tất cả
      for (let i = 0; i < this.totalPages; i++) {
        pages.push(i);
      }
    } else {
      // Luôn hiển thị trang đầu
      pages.push(0);
      
      let startPage = Math.max(1, this.currentPage - 1);
      let endPage = Math.min(this.totalPages - 2, this.currentPage + 1);
      
      // Điều chỉnh nếu gần đầu hoặc cuối
      if (this.currentPage <= 2) {
        endPage = Math.min(3, this.totalPages - 2);
      }
      if (this.currentPage >= this.totalPages - 3) {
        startPage = Math.max(1, this.totalPages - 4);
      }
      
      // Thêm ellipsis nếu cần
      if (startPage > 1) {
        pages.push(-1); // -1 represents ellipsis
      }
      
      // Thêm các trang ở giữa
      for (let i = startPage; i <= endPage; i++) {
        pages.push(i);
      }
      
      // Thêm ellipsis nếu cần
      if (endPage < this.totalPages - 2) {
        pages.push(-1);
      }
      
      // Luôn hiển thị trang cuối
      pages.push(this.totalPages - 1);
    }
    
    return pages;
  }

  // === Modal create user ===
  openCreateModal() {
    this.modalInstance = new bootstrap.Modal(this.createUserModal.nativeElement);
    this.createUserForm.reset({ roles: [] });
    this.modalInstance.show();
  }

  closeCreateModal() {
    if (this.modalInstance) this.modalInstance.hide();
  }

submitCreateUser() {
  if (this.createUserForm.invalid) return;

  const request: AdminCreateUserRequest = this.createUserForm.value;
  this.backendErrors = {}; // reset lỗi cũ

  this.userService.adminCreateUser(request).subscribe({
    next: res => {
      this.toastr.success('Tạo user thành công!', 'Thành công');
      this.loadUsers(this.currentPage); // reload bảng
      this.closeCreateModal();
    },
    error: err => {
      // Reset form validation
      Object.keys(this.createUserForm.controls).forEach(key => {
        this.createUserForm.get(key)?.markAsTouched();
      });

      if (err.error) {
        // 1. Nếu backend trả về errors object
        if (err.error.errors) {
          this.backendErrors = err.error.errors;

          // Hiển thị toast chi tiết từng field
          Object.entries(err.error.errors).forEach(([field, msg]: any) => {
            this.toastr.error(msg, `Lỗi ${field}`);
          });

        } 
        // 2. Nếu backend trả về message hoặc list
        else if (err.error.message) {
          this.toastr.error(err.error.message, 'Lỗi');
        } else if (err.error.details) {
          err.error.details.forEach((d: string) => this.toastr.error(d, 'Lỗi'));
        } else {
          this.toastr.error('Có lỗi xảy ra, vui lòng thử lại!', 'Lỗi');
        }
      } else {
        this.toastr.error('Có lỗi xảy ra, vui lòng thử lại!', 'Lỗi');
      }
    }
  });
}

  // === Search client-side ===
  filterUsers() {
    const text = this.searchText.toLowerCase();
    this.filteredUsers = this.users.filter(u =>
      u.username.toLowerCase().includes(text) ||
      (u.email?.toLowerCase().includes(text))
    );
  }

  exportUserReport() {
    this.loading = true;
    
    // Lấy username từ ô tìm kiếm (nếu có)
    const username = this.searchText.trim() || undefined;
    
    this.userService.exportUserReport(username).subscribe({
      next: (blob) => {
        const file = new Blob([blob], { type: 'application/pdf' });
        const fileURL = URL.createObjectURL(file);

        // Tự động mở PDF trong tab mới
        window.open(fileURL, '_blank');

        // Nếu muốn download thẳng thay vì mở tab:
        // const link = document.createElement('a');
        // link.href = fileURL;
        // link.download = 'users_report.pdf';
        // link.click();
        // URL.revokeObjectURL(fileURL);

        if (username) {
          this.toastr.success(`Xuất báo cáo PDF cho user "${username}" thành công!`, 'Thành công');
        } else {
          this.toastr.success('Xuất báo cáo PDF tất cả người dùng thành công!', 'Thành công');
        }
        
        this.loading = false;
      },
      error: (err) => {
        console.error('Export report error:', err);
        this.toastr.error('Không thể xuất báo cáo PDF!', 'Lỗi');
        this.loading = false;
      }
    });
  }

  openEditModal(user: any) {
    this.selectedUser = user;
    this.editUserForm.patchValue({
      firstName: user.firstName,
      lastName: user.lastName,
      status: user.status,
      verifyEmail: user.verifyEmail,
      roles: user.roles || []
    });

    this.editModalInstance = new bootstrap.Modal(this.editUserModal.nativeElement);
    this.editModalInstance.show();
  }

  closeEditModal() {
    if (this.editModalInstance) this.editModalInstance.hide();
  }

  submitEditUser() {
    if (this.editUserForm.invalid || !this.selectedUser) return;

    const request = this.editUserForm.value;
    this.userService.updateUser(this.selectedUser.id, request).subscribe({
      next: res => {
        this.toastr.success('Cập nhật người dùng thành công!', 'Thành công');
        this.closeEditModal();
        this.loadUsers(this.currentPage);
      },
      error: err => {
        console.error('Update error:', err);
        this.toastr.error(err.error?.message || 'Có lỗi xảy ra khi cập nhật!', 'Lỗi');
      }
    });
  }

  openDeleteModal(user: any) {
    this.userToDelete = user;
    this.deleteModalInstance = new bootstrap.Modal(this.deleteUserModal.nativeElement);
    this.deleteModalInstance.show();
  }

  closeDeleteModal() {
    if (this.deleteModalInstance) this.deleteModalInstance.hide();
    this.userToDelete = null;
  }

  confirmDeleteUser() {
    if (!this.userToDelete) return;

    this.userService.deleteUser(this.userToDelete.id).subscribe({
      next: (res) => {
        if (res.success) {
          this.toastr.success('Xóa người dùng thành công!', 'Thành công');
          this.closeDeleteModal();
          this.loadUsers(this.currentPage); // reload lại bảng
        } else {
          this.toastr.warning('Không thể xóa người dùng này!', 'Cảnh báo');
        }
      },
      error: (err) => {
        console.error('Error deleting user:', err);
        if (err.error?.message) {
          this.toastr.error(err.error.message, 'Lỗi');
        } else {
          this.toastr.error('Có lỗi xảy ra, vui lòng thử lại!', 'Lỗi');
        }
        this.closeDeleteModal();
      }
    });
  }

  openDetailsModal(userId: number) {
    this.userService.getUserDetails(userId).subscribe({
      next: res => {
        if (res.success) {
          this.selectedUserDetails = res.data;
          this.detailsModalInstance = new bootstrap.Modal(this.detailsUserModal.nativeElement);
          this.detailsModalInstance.show();
        } else {
          this.toastr.warning('Không tìm thấy thông tin người dùng', 'Cảnh báo');
        }
      },
      error: err => {
        console.error('Lỗi khi lấy chi tiết user:', err);
        this.toastr.error('Không thể tải thông tin chi tiết!', 'Lỗi');
      }
    });
  }

  closeDetailsModal() {
    if (this.detailsModalInstance) this.detailsModalInstance.hide();
  }

}
