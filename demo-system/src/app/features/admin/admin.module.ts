import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AdminRoutingModule } from './admin-routing.module';
import { DashboardComponent } from './dashboard/dashboard.component';
import { UsersComponent } from './users/users.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CategoryComponent } from './category/category.component';
import { ProductComponent } from './product/product.component';
import { PaymentsComponent } from './payments/payments.component';
import { AdminOrdersComponent } from './orders/orders.component';


@NgModule({
  declarations: [
    DashboardComponent,
    UsersComponent,
    CategoryComponent,
    ProductComponent,
    PaymentsComponent,
    AdminOrdersComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    AdminRoutingModule
  ]
})
export class AdminModule { }
