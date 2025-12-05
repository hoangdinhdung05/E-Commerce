import { Component, OnInit } from '@angular/core';
import { ToastService } from 'src/app/core/services/toast.service';

@Component({
  selector: 'app-toast',
  templateUrl: './toast.component.html',
  styleUrls: ['./toast.component.css']
})
export class ToastComponent implements OnInit {
  toasts: { message: string, type: string }[] = [];

  constructor(private toastService: ToastService) {}

  ngOnInit() {
    this.toastService.toastState$.subscribe(toast => {
      this.toasts.push(toast);
      setTimeout(() => this.removeToast(toast), 3000); // 3s auto close
    });
  }

  removeToast(toast: any) {
    this.toasts = this.toasts.filter(t => t !== toast);
  }
}
