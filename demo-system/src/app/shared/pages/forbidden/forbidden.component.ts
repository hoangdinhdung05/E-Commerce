import { Component } from '@angular/core';

@Component({
  selector: 'app-forbidden',
  template: `
    <div class="container py-5 text-center">
      <h1 class="mb-3">403 - Forbidden</h1>
      <p>Bạn không có quyền truy cập trang này.</p>
      <a routerLink="/" class="btn btn-primary mt-3">Về trang chủ</a>
    </div>
  `
})
export class ForbiddenComponent {}
