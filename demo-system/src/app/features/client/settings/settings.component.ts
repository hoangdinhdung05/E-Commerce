import { Component, OnInit } from '@angular/core';
import { ToastService } from '../../../core/services/toast.service';

interface NotificationSettings {
  email: boolean;
  sms: boolean;
  push: boolean;
  marketing: boolean;
  orderUpdates: boolean;
  promotions: boolean;
  newsletter: boolean;
}

interface PrivacySettings {
  profileVisibility: 'public' | 'private' | 'friends';
  showEmail: boolean;
  showPhone: boolean;
  allowMessages: boolean;
  allowFriendRequests: boolean;
}

interface DisplaySettings {
  theme: 'light' | 'dark' | 'auto';
  language: string;
  currency: string;
  timezone: string;
  itemsPerPage: number;
}

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.css']
})
export class SettingsComponent implements OnInit {
  
  // Settings
  notificationSettings: NotificationSettings = {
    email: true,
    sms: false,
    push: true,
    marketing: false,
    orderUpdates: true,
    promotions: false,
    newsletter: false
  };

  privacySettings: PrivacySettings = {
    profileVisibility: 'public',
    showEmail: false,
    showPhone: false,
    allowMessages: true,
    allowFriendRequests: true
  };

  displaySettings: DisplaySettings = {
    theme: 'light',
    language: 'vi',
    currency: 'VND',
    timezone: 'Asia/Ho_Chi_Minh',
    itemsPerPage: 20
  };

  // State
  isLoading = false;
  isSaving = false;
  activeTab = 'notifications';

  // Options
  languageOptions = [
    { value: 'vi', label: 'Tiếng Việt' },
    { value: 'en', label: 'English' }
  ];

  currencyOptions = [
    { value: 'VND', label: 'Vietnamese Dong (₫)' },
    { value: 'USD', label: 'US Dollar ($)' }
  ];

  timezoneOptions = [
    { value: 'Asia/Ho_Chi_Minh', label: '(UTC+07:00) Ho Chi Minh' },
    { value: 'Asia/Bangkok', label: '(UTC+07:00) Bangkok' },
    { value: 'Asia/Singapore', label: '(UTC+08:00) Singapore' }
  ];

  itemsPerPageOptions = [10, 20, 50, 100];

  constructor(
    private toastService: ToastService
  ) { }

  ngOnInit(): void {
    this.loadSettings();
  }

  loadSettings(): void {
    this.isLoading = true;
    
    // Mock API call - Replace with actual API
    setTimeout(() => {
      // Settings would be loaded from API
      this.isLoading = false;
    }, 1000);
  }

  saveSettings(): void {
    this.isSaving = true;
    
    const settings = {
      notifications: this.notificationSettings,
      privacy: this.privacySettings,
      display: this.displaySettings
    };

    console.log('Saving settings:', settings);
    
    // Mock API call - Replace with actual API
    setTimeout(() => {
      this.toastService.success('Cài đặt đã được lưu thành công');
      this.isSaving = false;
    }, 2000);
  }

  resetToDefault(): void {
    if (confirm('Bạn có chắc chắn muốn khôi phục cài đặt mặc định?')) {
      this.notificationSettings = {
        email: true,
        sms: false,
        push: true,
        marketing: false,
        orderUpdates: true,
        promotions: false,
        newsletter: false
      };

      this.privacySettings = {
        profileVisibility: 'public',
        showEmail: false,
        showPhone: false,
        allowMessages: true,
        allowFriendRequests: true
      };

      this.displaySettings = {
        theme: 'light',
        language: 'vi',
        currency: 'VND',
        timezone: 'Asia/Ho_Chi_Minh',
        itemsPerPage: 20
      };

      this.toastService.success('Đã khôi phục cài đặt mặc định');
    }
  }

  exportSettings(): void {
    const settings = {
      notifications: this.notificationSettings,
      privacy: this.privacySettings,
      display: this.displaySettings,
      exportedAt: new Date().toISOString()
    };

    const dataStr = JSON.stringify(settings, null, 2);
    const dataBlob = new Blob([dataStr], { type: 'application/json' });
    const url = URL.createObjectURL(dataBlob);
    
    const link = document.createElement('a');
    link.href = url;
    link.download = 'settings-backup.json';
    link.click();
    
    URL.revokeObjectURL(url);
    this.toastService.success('Đã xuất cài đặt thành công');
  }

  importSettings(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const file = input.files[0];
      const reader = new FileReader();
      
      reader.onload = (e) => {
        try {
          const settings = JSON.parse(e.target?.result as string);
          
          if (settings.notifications) this.notificationSettings = settings.notifications;
          if (settings.privacy) this.privacySettings = settings.privacy;
          if (settings.display) this.displaySettings = settings.display;
          
          this.toastService.success('Đã nhập cài đặt thành công');
        } catch (error) {
          this.toastService.error('File cài đặt không hợp lệ');
        }
      };
      
      reader.readAsText(file);
    }
  }

  setActiveTab(tab: string): void {
    this.activeTab = tab;
  }

  // Theme methods
  applyTheme(): void {
    // Apply theme logic here
    console.log('Applying theme:', this.displaySettings.theme);
  }

  // Test notification
  testNotification(): void {
    if ('Notification' in window) {
      Notification.requestPermission().then(permission => {
        if (permission === 'granted') {
          new Notification('Demo Store', {
            body: 'Đây là thông báo thử nghiệm!',
            icon: '/assets/logo.png'
          });
          this.toastService.success('Đã gửi thông báo thử nghiệm');
        }
      });
    } else {
      this.toastService.error('Trình duyệt không hỗ trợ thông báo');
    }
  }

  // Clear data
  clearCache(): void {
    if (confirm('Bạn có chắc chắn muốn xóa bộ nhớ đệm?')) {
      // Clear cache logic
      localStorage.removeItem('demo-store-cache');
      sessionStorage.clear();
      this.toastService.success('Đã xóa bộ nhớ đệm');
    }
  }

  clearHistory(): void {
    if (confirm('Bạn có chắc chắn muốn xóa lịch sử duyệt web?')) {
      // Clear history logic
      this.toastService.success('Đã xóa lịch sử duyệt web');
    }
  }
}