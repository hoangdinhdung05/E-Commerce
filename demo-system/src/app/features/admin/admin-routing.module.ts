import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from './dashboard/dashboard.component';
import { UsersComponent } from './users/users.component';
import { CategoryComponent } from './category/category.component';
import { ProductComponent } from './product/product.component';
import { PaymentsComponent } from './payments/payments.component';
import { AdminOrdersComponent } from './orders/orders.component';

const routes: Routes = [
  {path: '', component: DashboardComponent},
  {path: 'users', component: UsersComponent},
  {path: 'categories', component: CategoryComponent},
  {path: 'products', component: ProductComponent},
  {path: 'orders', component: AdminOrdersComponent},
  {path: 'payments', component: PaymentsComponent},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AdminRoutingModule { }

 