import { Component } from '@angular/core';

@Component({
  selector: 'app-client-layout',
  template: `
    <app-client-header></app-client-header>
    <main class="main-content">
      <router-outlet></router-outlet>
    </main>
    <app-client-footer></app-client-footer>
  `,
  styles: [`
    .main-content {
      min-height: calc(100vh - 200px);
    }
  `]
})
export class ClientLayoutComponent { }