import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { takeUntil, switchMap } from 'rxjs/operators';
import { ProductService } from '../../../core/services/products/product.service';
import { ProductResponse } from '../../../core/models/response/Product/ProductResponse';
import { CartService } from '../../../core/services/cart/cart.service';
import { environment } from '../../../../environments/environment';
import { ToastrService } from 'ngx-toastr';
import { DestroyableComponent } from '../../../shared/components/base/destroyable.component';

@Component({
  selector: 'app-product-detail',
  templateUrl: './product-detail.component.html',
  styleUrls: ['./product-detail.component.css']
})
export class ProductDetailComponent extends DestroyableComponent implements OnInit {
  product: ProductResponse | null = null;
  isLoading = false;
  quantity = 1;
  selectedImage = 0;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    private cartService: CartService,
    private toastr: ToastrService
  ) {
    super();
  }

  ngOnInit(): void {
    this.route.params.pipe(
      switchMap(params => {
        const id = +params['id'];
        this.isLoading = true;
        return this.productService.getById(id);
      }),
      takeUntil(this.destroy$)
    ).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.product = response.data;
        } else {
          this.toastr.error('Không tìm thấy sản phẩm!', 'Lỗi');
          this.router.navigate(['/']);
        }
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading product details:', error);
        this.toastr.error('Không thể tải thông tin sản phẩm!', 'Lỗi');
        this.router.navigate(['/']);
        this.isLoading = false;
      }
    });
  }

  getProductImageUrl(product: ProductResponse): string {
    if (!product.productImageUrl) {
      return 'https://via.placeholder.com/600x600/007bff/ffffff?text=No+Image';
    }
    return `${environment.assetBase}${product.productImageUrl}`;
  }

  increaseQuantity(): void {
    if (this.product && this.quantity < this.product.quantity) {
      this.quantity++;
    }
  }

  decreaseQuantity(): void {
    if (this.quantity > 1) {
      this.quantity--;
    }
  }

  addToCart(): void {
    if (!this.product) return;
    
    this.cartService.addToCart({
      productId: this.product.id,
      quantity: this.quantity
    })
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: (response) => {
        this.toastr.success(`Đã thêm ${this.quantity} sản phẩm vào giỏ hàng!`, 'Thành công');
      },
      error: (error) => {
        console.error('Error adding to cart:', error);
        this.toastr.error('Không thể thêm sản phẩm vào giỏ hàng!', 'Lỗi');
      }
    });
  }

  buyNow(): void {
    if (!this.product) return;
    
    this.cartService.addToCart({
      productId: this.product.id,
      quantity: this.quantity
    })
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: (response) => {
        this.router.navigate(['/checkout']);
      },
      error: (error) => {
        console.error('Error adding to cart:', error);
        this.toastr.error('Không thể thêm sản phẩm vào giỏ hàng!', 'Lỗi');
      }
    });
  }

  isInStock(): boolean {
    return this.product ? this.product.quantity > 0 : false;
  }

  getStarArray(rating: number = 4.5): number[] {
    return Array(5).fill(0).map((_, i) => i < Math.floor(rating) ? 1 : 0);
  }

  goBack(): void {
    this.router.navigate(['/']);
  }
}
