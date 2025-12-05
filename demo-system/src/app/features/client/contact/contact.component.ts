import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { UserService } from '../../../core/services/users/user.service';
import { UserDetailsResponse } from '../../../core/models/response/User/UserDetailsRespomse';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-contact',
  templateUrl: './contact.component.html',
  styleUrls: ['./contact.component.css']
})
export class ContactComponent implements OnInit {
  contactForm!: FormGroup;
  isSubmitting = false;
  isLoadingAdmin = true;
  adminUser: UserDetailsResponse | null = null;

  // Thông tin liên hệ từ admin
  contactInfo = {
    address: '123 Đường ABC, Quận 1, TP. Hồ Chí Minh, Việt Nam',
    phone: '',
    email: '',
    workingHours: 'Thứ 2 - Thứ 6: 8:00 - 18:00<br>Thứ 7: 8:00 - 12:00<br>Chủ nhật: Nghỉ',
    socialLinks: {
      facebook: 'https://facebook.com/demostore',
      instagram: 'https://instagram.com/demostore',
      twitter: 'https://twitter.com/demostore',
      youtube: 'https://youtube.com/demostore'
    }
  };

  departments = [
    {
      icon: 'fa-shopping-cart',
      title: 'Bán hàng',
      description: 'Tư vấn và hỗ trợ mua hàng',
      email: 'sales@demostore.com',
      phone: '+84 123 456 789'
    },
    {
      icon: 'fa-headset',
      title: 'Chăm sóc khách hàng',
      description: 'Hỗ trợ sau bán hàng',
      email: 'support@demostore.com',
      phone: '+84 987 654 321'
    },
    {
      icon: 'fa-handshake',
      title: 'Hợp tác kinh doanh',
      description: 'Đối tác và hợp tác',
      email: 'partner@demostore.com',
      phone: '+84 456 789 123'
    },
    {
      icon: 'fa-envelope',
      title: 'Góp ý',
      description: 'Ý kiến và khiếu nại',
      email: 'feedback@demostore.com',
      phone: '+84 321 654 987'
    }
  ];

  faqs = [
    {
      question: 'Làm thế nào để đặt hàng?',
      answer: 'Bạn có thể đặt hàng trực tiếp trên website hoặc liên hệ hotline để được hỗ trợ đặt hàng.'
    },
    {
      question: 'Chính sách giao hàng như thế nào?',
      answer: 'Chúng tôi giao hàng toàn quốc trong vòng 3-5 ngày làm việc. Miễn phí ship cho đơn hàng trên 500.000đ.'
    },
    {
      question: 'Tôi có thể đổi trả hàng không?',
      answer: 'Chúng tôi hỗ trợ đổi trả trong vòng 7 ngày với sản phẩm còn nguyên tem, mác và chưa qua sử dụng.'
    },
    {
      question: 'Các hình thức thanh toán?',
      answer: 'Chúng tôi chấp nhận thanh toán COD, chuyển khoản ngân hàng, và các ví điện tử như MoMo, ZaloPay.'
    }
  ];

  constructor(
    private fb: FormBuilder,
    private toastr: ToastrService,
    private userService: UserService
  ) { }

  ngOnInit(): void {
    this.initForm();
    this.loadAdminContact();
  }

  loadAdminContact(): void {
    this.isLoadingAdmin = true;
    // Lấy danh sách users và tìm admin đầu tiên (giả sử ID = 1 hoặc username = admin)
    // Vì UserResponse không có roles, ta sẽ dùng getUserDetails với ID cố định
    this.userService.getUserDetails(1).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.adminUser = response.data;
          // Cập nhật thông tin liên hệ từ admin
          if (this.adminUser.email) {
            this.contactInfo.email = this.adminUser.email;
            this.contactInfo.phone = '+84 123 456 789'; // Hardcode vì API không có phone
          }
        }
        this.isLoadingAdmin = false;
      },
      error: (error) => {
        console.error('Error loading admin contact:', error);
        this.isLoadingAdmin = false;
        // Giữ giá trị mặc định nếu lỗi
        this.contactInfo.phone = '+84 123 456 789';
        this.contactInfo.email = 'contact@demostore.com';
      }
    });
  }

  initForm(): void {
    this.contactForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
      subject: ['', [Validators.required]],
      message: ['', [Validators.required, Validators.minLength(10)]]
    });
  }

  onSubmit(): void {
    if (this.contactForm.invalid) {
      Object.keys(this.contactForm.controls).forEach(key => {
        this.contactForm.get(key)?.markAsTouched();
      });
      this.toastr.warning('Vui lòng điền đầy đủ thông tin!', 'Cảnh báo');
      return;
    }

    this.isSubmitting = true;

    // Mock API call - sẽ thay bằng service sau
    setTimeout(() => {
      console.log('Form data:', this.contactForm.value);
      this.toastr.success('Cảm ơn bạn đã liên hệ! Chúng tôi sẽ phản hồi sớm nhất.', 'Thành công');
      this.contactForm.reset();
      this.isSubmitting = false;
    }, 1500);
  }

  getFieldError(fieldName: string): string {
    const field = this.contactForm.get(fieldName);
    if (field?.touched && field?.errors) {
      if (field.errors['required']) return 'Trường này là bắt buộc';
      if (field.errors['email']) return 'Email không hợp lệ';
      if (field.errors['minlength']) return `Tối thiểu ${field.errors['minlength'].requiredLength} ký tự`;
      if (field.errors['pattern']) return 'Số điện thoại không hợp lệ (10 chữ số)';
    }
    return '';
  }
}
