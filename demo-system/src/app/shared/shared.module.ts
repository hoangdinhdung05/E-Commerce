import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HeaderComponent } from './header/header.component';
import { FooterComponent } from './footer/footer.component';
import { SideBarComponent } from './side-bar/side-bar.component';
import { ToastComponent } from './toast/toast.component';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ForbiddenComponent } from './pages/forbidden/forbidden.component';
import { NotFoundComponent } from './pages/not-found/not-found.component';

@NgModule({
  declarations: [
    HeaderComponent,
    FooterComponent,
    SideBarComponent,
    ToastComponent,
    ForbiddenComponent,
  NotFoundComponent,
  ],
  imports: [
    CommonModule,
    FormsModule,
    RouterModule
  ],
  exports: [ 
    HeaderComponent,
    FooterComponent,
    SideBarComponent,
    ToastComponent
  ]
})
export class SharedModule { }
