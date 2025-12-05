import { Component, OnInit, Input, OnChanges, SimpleChanges } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { takeUntil } from 'rxjs/operators';
import { ProductService } from '../../../../core/services/products/product.service';
import { CartService } from '../../../../core/services/cart/cart.service';
import { AuthService } from '../../../../core/auth.service';
import { ProductResponse } from '../../../../core/models/response/Product/ProductResponse';
import { AddToCartRequest } from '../../../../core/models/request/Cart/AddToCartRequest';
import { environment } from '../../../../../environments/environment';
import { ToastrService } from 'ngx-toastr';
import { FilterOptions } from '../product-filter/product-filter.component';
import { CategoryService } from '../../../../core/services/categories/category.service';
import { forkJoin } from 'rxjs';
import { DestroyableComponent } from '../../../../shared/components/base/destroyable.component';

@Component({
  selector: 'app-product-list',
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.css']
})
export class ProductListComponent extends DestroyableComponent implements OnInit, OnChanges {
  @Input() filterOptions: FilterOptions | null = null;
  
  allProducts: ProductResponse[] = [];
  displayedProducts: ProductResponse[] = [];
  viewMode: 'grid' | 'list' = 'grid';
  isLoading = false;
  
  // Pagination
  currentPage = 0;
  pageSize = 12; 
  totalElements = 0;
  totalPages = 0;

  isSearching = false;
  searchQuery = '';


  constructor(
    private productService: ProductService,
    private cartService: CartService,
    private authService: AuthService,
    private toastr: ToastrService,
    private route: ActivatedRoute,
    private router: Router,
    private categoryService: CategoryService
  ) {
    super();
  }

  ngOnInit(): void {
    // Listen to query params changes
    this.route.queryParams
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
        const searchQuery = params['search'];
        if (searchQuery) {
          this.searchProducts(searchQuery);
        } else if (!this.filterOptions || this.filterOptions.categories.length === 0) {
          // Only load all products if no filters are active
          this.loadProducts();
        } else {
          // Load with filters
          this.loadProductsWithFilters();
        }
      });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['filterOptions'] && !changes['filterOptions'].firstChange) {
      const options = changes['filterOptions'].currentValue;
      if (options) {
        // Reset to first page when filters change
        this.currentPage = 0;
        this.loadProductsWithFilters();
      }
    }
  }

  loadProducts(page: number = 0): void {
    this.isLoading = true;
    this.currentPage = page;
    console.log('Loading products, page:', this.currentPage, 'pageSize:', this.pageSize);
    
    this.productService.getAll(this.currentPage, this.pageSize)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
      next: (response: any) => {
        console.log('Product service response:', response);
        if (response.success && response.data) {
          console.log('Products loaded:', response.data.content);
          this.allProducts = response.data.content;
          this.displayedProducts = [...this.allProducts];
          this.totalElements = response.data.totalElements;
          this.totalPages = response.data.totalPages;

          // Debug: Log detailed product structure
          if (this.allProducts.length > 0) {
            console.log('First product structure:', JSON.stringify(this.allProducts[0], null, 2));
            console.log('Product category:', this.allProducts[0].category);
            console.log('Product price:', this.allProducts[0].price, typeof this.allProducts[0].price);
          }

          console.log("List of all products:", this.allProducts);
          console.log('Total elements:', this.totalElements, 'Total pages:', this.totalPages);
        } else {
          console.error('API response not successful or no data:', response);
        }
        this.isLoading = false;
      },
      error: (error: any) => {
        console.error('Error loading products:', error);
        this.isLoading = false;
      }
    });
  }

  loadProductsWithFilters(page: number = 0): void {
    if (!this.filterOptions) {
      this.loadProducts(page);
      return;
    }

    // If no category filter, load all products
    if (this.filterOptions.categories.length === 0) {
      this.loadProducts(page);
      return;
    }

    this.isLoading = true;
    this.currentPage = page;
    this.isSearching = false;

    // Load categories first to get category names
    this.categoryService.getAll(0, 10)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
      next: (catResponse) => {
        if (!catResponse.success || !catResponse.data) {
          this.loadProducts(page);
          return;
        }

        const categories = catResponse.data.content;
        const categoryRequests = this.filterOptions!.categories
          .map(categoryId => categories.find(c => c.id === categoryId))
          .filter(category => category !== undefined)
          .map(category => this.productService.searchByCategory(category!.name, this.currentPage, this.pageSize));

        if (categoryRequests.length === 0) {
          this.displayedProducts = [];
          this.totalElements = 0;
          this.totalPages = 0;
          this.isLoading = false;
          return;
        }

        // For single category, use its pagination directly
        if (categoryRequests.length === 1) {
          categoryRequests[0]
            .pipe(takeUntil(this.destroy$))
            .subscribe({
            next: (response) => {
              if (response.success && response.data) {
                const pageData = response.data as any;
                this.displayedProducts = pageData.content || [];
                this.totalElements = pageData.totalElements || 0;
                this.totalPages = pageData.totalPages || 0;
              }
              this.isLoading = false;
            },
            error: (error) => {
              console.error('Error loading products:', error);
              this.displayedProducts = [];
              this.isLoading = false;
            }
          });
        } else {
          // For multiple categories, merge results (pagination becomes approximate)
          forkJoin(categoryRequests)
            .pipe(takeUntil(this.destroy$))
            .subscribe({
            next: (responses) => {
              const allProducts: ProductResponse[] = [];
              let totalAvailable = 0;

              responses.forEach(response => {
                if (response.success && response.data) {
                  const pageData = response.data as any;
                  const products = pageData.content || [];
                  allProducts.push(...products);
                  totalAvailable += pageData.totalElements || 0;
                }
              });

              // Remove duplicates
              const uniqueProducts = Array.from(
                new Map(allProducts.map(p => [p.id, p])).values()
              );

              this.displayedProducts = uniqueProducts;
              this.totalElements = totalAvailable;
              this.totalPages = Math.ceil(totalAvailable / this.pageSize);
              this.isLoading = false;
            },
            error: (error) => {
              console.error('Error loading products:', error);
              this.displayedProducts = [];
              this.isLoading = false;
            }
          });
        }
      },
      error: (error) => {
        console.error('Error loading categories:', error);
        this.loadProducts(page);
      }
    });
  }

  nextPage(): void { 
    if (this.currentPage < this.totalPages - 1) {
      if (this.filterOptions && this.filterOptions.categories.length > 0) {
        this.loadProductsWithFilters(this.currentPage + 1);
      } else {
        this.loadProducts(this.currentPage + 1);
      }
    }
  }
  
  previousPage(): void { 
    if (this.currentPage > 0) {
      if (this.filterOptions && this.filterOptions.categories.length > 0) {
        this.loadProductsWithFilters(this.currentPage - 1);
      } else {
        this.loadProducts(this.currentPage - 1);
      }
    }
  }
  
  goToPage(page: number): void { 
    if (page >= 0 && page < this.totalPages) {
      if (this.filterOptions && this.filterOptions.categories.length > 0) {
        this.loadProductsWithFilters(page);
      } else {
        this.loadProducts(page);
      }
    }
  }

  // Helper method to get page numbers to display
  getPageNumbers(): number[] {
    const maxPagesToShow = 5;
    const pages: number[] = [];
    
    if (this.totalPages <= maxPagesToShow) {
      // Show all pages if total is small
      for (let i = 0; i < this.totalPages; i++) {
        pages.push(i);
      }
    } else {
      // Show pages with ellipsis
      const halfMax = Math.floor(maxPagesToShow / 2);
      let startPage = Math.max(0, this.currentPage - halfMax);
      let endPage = Math.min(this.totalPages - 1, startPage + maxPagesToShow - 1);
      
      // Adjust startPage if we're near the end
      if (endPage - startPage < maxPagesToShow - 1) {
        startPage = Math.max(0, endPage - maxPagesToShow + 1);
      }
      
      // Always show first page
      if (startPage > 0) {
        pages.push(0);
        if (startPage > 1) {
          pages.push(-1); // -1 represents ellipsis
        }
      }
      
      // Show middle pages
      for (let i = startPage; i <= endPage; i++) {
        pages.push(i);
      }
      
      // Always show last page
      if (endPage < this.totalPages - 1) {
        if (endPage < this.totalPages - 2) {
          pages.push(-1); // -1 represents ellipsis
        }
        pages.push(this.totalPages - 1);
      }
    }
    
    return pages;
  }

  toggleViewMode(): void {
    this.viewMode = this.viewMode === 'grid' ? 'list' : 'grid';
  }

  addToCart(product: ProductResponse): void {
    // Check if user is logged in
    if (!this.authService.isAuthenticated()) {
      this.toastr.warning('Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng', 'Yêu cầu đăng nhập');
      this.router.navigate(['/auth/login']);
      return;
    }

    // Check if product is in stock
    if (!this.isProductInStock(product)) {
      this.toastr.error('Sản phẩm hiện đang hết hàng', 'Không thể thêm vào giỏ');
      return;
    }

    const request: AddToCartRequest = {
      productId: product.id,
      quantity: 1
    };

    this.cartService.addToCart(request)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
      next: (response) => {
        if (response.success) {
          this.toastr.success(`Đã thêm "${product.name}" vào giỏ hàng`, 'Thành công');
          // Cart will be automatically updated via cart$ observable in CartService
        } else {
          this.toastr.error('Không thể thêm sản phẩm vào giỏ hàng', 'Lỗi');
        }
      },
      error: (error) => {
        console.error('Error adding to cart:', error);
        const errorMessage = error.error?.message || 'Không thể thêm sản phẩm vào giỏ hàng';
        this.toastr.error(errorMessage, 'Lỗi');
      }
    });
  }

  viewProductDetails(product: ProductResponse): void {
    this.router.navigate(['/product', product.id]);
  }

  getProductImageUrl(product: ProductResponse): string {
    if (!product.productImageUrl) {
      return 'https://via.placeholder.com/300x300/007bff/ffffff?text=No+Image';
    }
    
    // Use the same approach as admin page
    return `${environment.assetBase}${product.productImageUrl.startsWith('/') ? '' : '/'}${product.productImageUrl}`;
  }

  isProductInStock(product: ProductResponse): boolean {
    return product.quantity > 0;
  }

  getStarArray(rating: number = 4.5): number[] {
    return Array(5).fill(0).map((_, i) => i < Math.floor(rating) ? 1 : 0);
  }

  formatPrice(price: number): string {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(price);
  }

  searchProducts(name: string): void {
    if (!name.trim()) {
      // Nếu rỗng → load lại toàn bộ danh sách mặc định
      this.isSearching = false;
      this.currentPage = 0; // Reset về trang đầu
      this.loadProducts();
      return;
    }

    this.isLoading = true;
    this.isSearching = true;
    this.searchQuery = name;

    this.productService.searchByName(name)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.displayedProducts = res.data;
          this.totalElements = res.data.length;
          this.totalPages = 0; // Không có phân trang khi search
          this.currentPage = 0;
        } else {
          this.displayedProducts = [];
          this.totalElements = 0;
          this.totalPages = 0;
        }
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Search error:', err);
        this.displayedProducts = [];
        this.totalElements = 0;
        this.totalPages = 0;
        this.isLoading = false;
      }
    });
  }
}