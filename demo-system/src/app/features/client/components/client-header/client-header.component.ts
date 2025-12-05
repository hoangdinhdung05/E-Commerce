import { Component, OnInit, HostListener } from '@angular/core';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { AuthService } from '../../../../core/auth.service';
import { UserService } from '../../../../core/services/users/user.service';
import { CategoryService } from '../../../../core/services/categories/category.service';
import { ProductService } from '../../../../core/services/products/product.service';
import { CartService } from '../../../../core/services/cart/cart.service';
import { UserDetailsResponse } from '../../../../core/models/response/User/UserDetailsRespomse';
import { CategoryResponse } from '../../../../core/models/response/Category/CategoryResponse';
import { ProductResponse } from '../../../../core/models/response/Product/ProductResponse';
import { CartResponse } from '../../../../core/models/response/Cart/CartResponse';
import { CartItemResponse } from '../../../../core/models/response/Cart/CartItemResponse';
import { environment } from '../../../../../environments/environment';

@Component({
  selector: 'app-client-header',
  templateUrl: './client-header.component.html',
  styleUrls: ['./client-header.component.css']
})
export class ClientHeaderComponent implements OnInit {
  isLoggedIn = false;
  user: UserDetailsResponse | null = null;
  isMenuOpen = false;
  avatarUrl: string | null = null;
  userInitial = '';
  isLoadingUser = false;
  
  // Categories dropdown
  categories: CategoryResponse[] = [];
  isCategoriesOpen = false;
  isLoadingCategories = false;
  
  // Search functionality
  searchSuggestions: ProductResponse[] = [];
  searchQuery$ = new Subject<string>();
  isSearching = false;
  showSuggestions = false;

  // Cart functionality
  cartCount = 0;
  cart: CartResponse | null = null;
  isCartDropdownOpen = false;
  isLoadingCart = false;

  constructor(
    private authService: AuthService,
    private userService: UserService,
    private categoryService: CategoryService,
    private productService: ProductService,
    private cartService: CartService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.checkAuthStatus();
    this.loadCategories();
    this.setupSearchAutocomplete();
    this.subscribeToCart();
  }

  subscribeToCart(): void {
    // Subscribe to cart changes from CartService
    this.cartService.cart$.subscribe({
      next: (cart: CartResponse | null) => {
        this.cart = cart;
        this.cartCount = cart?.totalItems || 0;
      },
      error: (error: any) => {
        console.error('Error subscribing to cart:', error);
      }
    });

    // Load initial cart data if user is logged in
    if (this.isLoggedIn) {
      this.loadCart();
    }
  }

  loadCart(): void {
    this.isLoadingCart = true;
    this.cartService.getCart().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.cart = response.data;
          this.cartCount = this.cart.totalItems;
        }
        this.isLoadingCart = false;
      },
      error: (error) => {
        console.error('Error loading cart:', error);
        this.isLoadingCart = false;
      }
    });
  }

  setupSearchAutocomplete(): void {
    this.searchQuery$.pipe(
      debounceTime(300), // Đợi 300ms sau khi user ngừng gõ
      distinctUntilChanged(), // Chỉ search nếu giá trị thay đổi
      switchMap(query => {
        if (!query || query.trim().length < 2) {
          this.searchSuggestions = [];
          this.showSuggestions = false;
          return [];
        }
        this.isSearching = true;
        return this.productService.searchByName(query.trim());
      })
    ).subscribe({
      next: (response) => {
        if (response && response.success && response.data) {
          this.searchSuggestions = response.data.slice(0, 5); // Giới hạn 5 gợi ý
          this.showSuggestions = this.searchSuggestions.length > 0;
        } else {
          this.searchSuggestions = [];
          this.showSuggestions = false;
        }
        this.isSearching = false;
      },
      error: (error) => {
        console.error('Search error:', error);
        this.searchSuggestions = [];
        this.showSuggestions = false;
        this.isSearching = false;
      }
    });
  }

  @HostListener('document:click', ['$event'])
  onClickOutside(event: Event): void {
    const target = event.target as HTMLElement;
    
    // Check if click is outside user menu
    if (this.isMenuOpen && !target.closest('.user-menu')) {
      this.isMenuOpen = false;
    }
    
    // Check if click is outside categories menu
    if (this.isCategoriesOpen && !target.closest('.categories-menu')) {
      this.isCategoriesOpen = false;
    }

    // Check if click is outside search suggestions
    if (this.showSuggestions && !target.closest('.search-section')) {
      this.showSuggestions = false;
    }

    // Check if click is outside cart dropdown
    if (this.isCartDropdownOpen && !target.closest('.cart-section')) {
      this.isCartDropdownOpen = false;
    }
  }

  checkAuthStatus(): void {
    this.isLoggedIn = this.authService.isAuthenticated();
    if (this.isLoggedIn) {
      this.loadCurrentUser();
    }
  }

  loadCategories(): void {
    this.isLoadingCategories = true;
    this.categoryService.getAllCategories(0, 20).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.categories = response.data.content;
        }
        this.isLoadingCategories = false;
      },
      error: (error) => {
        console.error('Error loading categories:', error);
        this.isLoadingCategories = false;
      }
    });
  }

  loadCurrentUser(): void {
    this.isLoadingUser = true;
    this.userService.getCurrentUser().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.user = response.data;
          this.avatarUrl = this.getAvatarUrl(this.user.avatarUrl);
          this.userInitial = this.user.firstName?.charAt(0) || this.user.username?.charAt(0) || 'U';
        }
        this.isLoadingUser = false;
      },
      error: (error) => {
        console.error('Error loading current user:', error);
        // Fallback to basic user info from token
        const userId = this.authService.getUserId();
        const roles = this.authService.getRoles();
        if (userId) {
          this.user = { 
            id: userId, 
            username: 'User',
            firstName: 'User',
            lastName: '',
            email: '',
            status: '',
            verifyEmail: false,
            avatarUrl: ''
          };
          this.userInitial = 'U';
        }
        this.isLoadingUser = false;
      }
    });
  }

  getAvatarUrl(avatarUrl?: string): string | null {
    if (!avatarUrl) {
      return null;
    }
    
    // Use the same approach as admin page
    return `${environment.assetBase}${avatarUrl.startsWith('/') ? '' : '/'}${avatarUrl}`;
  }

  navigateToLogin(): void {
    this.router.navigate(['/auth/login']);
  }

  navigateToRegister(): void {
    this.router.navigate(['/auth/register']);
  }

  navigateToHome(): void {
    this.router.navigate(['/']);
  }

  navigateToCategory(): void {
    this.router.navigate(['/category']);
  }

  navigateToCategoryById(categoryId: number): void {
    this.router.navigate(['/category', categoryId]);
  }

  navigateToAbout(): void {
    this.router.navigate(['/about']);
  }

  navigateToContact(): void {
    this.router.navigate(['/contact']);
  }

  logout(): void {
    this.authService.logout();
    this.isLoggedIn = false;
    this.user = null;
    this.isMenuOpen = false;
    this.router.navigate(['/']);
  }

  viewProfile(): void {
    this.router.navigate(['/profile']);
    this.isMenuOpen = false;
  }

  changePassword(): void {
    this.router.navigate(['/change-password']);
    this.isMenuOpen = false;
  }

  viewOrders(): void {
    this.router.navigate(['/orders']);
    this.isMenuOpen = false;
  }

  viewWishlist(): void {
    // TODO: Navigate to wishlist page
    this.isMenuOpen = false;
    console.log('Navigate to wishlist');
  }

  viewAddresses(): void {
    // TODO: Navigate to addresses page
    this.isMenuOpen = false;
    console.log('Navigate to addresses');
  }

  viewPaymentMethods(): void {
    // TODO: Navigate to payment methods page
    this.isMenuOpen = false;
    console.log('Navigate to payment methods');
  }

  viewNotifications(): void {
    // TODO: Navigate to notifications page
    this.isMenuOpen = false;
    console.log('Navigate to notifications');
  }

  viewSettings(): void {
    this.router.navigate(['/settings']);
    this.isMenuOpen = false;
  }

  // Search functionality
  onSearchInput(query: string): void {
    this.searchQuery$.next(query);
  }

  performSearch(query: string): void {
    if (!query || query.trim() === '') return;
    // Navigate to category page with search query
    this.router.navigate(['/category'], { 
      queryParams: { search: query.trim() } 
    });
    this.showSuggestions = false;
  }

  selectSuggestion(product: ProductResponse): void {
    this.performSearch(product.name);
    this.showSuggestions = false;
  }

  getProductImageUrl(product: ProductResponse): string {
    if (!product.productImageUrl) {
      return 'https://via.placeholder.com/50x50/007bff/ffffff?text=No+Image';
    }
    return `${environment.assetBase}${product.productImageUrl.startsWith('/') ? '' : '/'}${product.productImageUrl}`;
  }

  // Categories dropdown methods
  toggleCategoriesMenu(): void {
    this.isCategoriesOpen = !this.isCategoriesOpen;
  }

  onCategoriesMouseEnter(): void {
    this.isCategoriesOpen = true;
  }

  onCategoriesMouseLeave(): void {
    // Small delay to allow moving to dropdown
    setTimeout(() => {
      this.isCategoriesOpen = false;
    }, 200);
  }

  // === mở/đóng theo hover + click ===
  openUserMenu(): void {
    this.isMenuOpen = true;
  }
  closeUserMenu(): void {
    this.isMenuOpen = false;
  }
  toggleUserMenu(event: MouseEvent): void {
    event.stopPropagation();
    this.isMenuOpen = !this.isMenuOpen;
  }

  // Đóng khi click ngoài: bạn đã có HostListener document:click – ok

  // === gom action về 1 chỗ, tự đóng menu sau khi xử lý ===
  onAction(action: string): void {
    switch (action) {
      case 'profile':        this.viewProfile(); break;
      case 'orders':         this.viewOrders(); break;
      case 'wishlist':       this.viewWishlist(); break;
      case 'addresses':      this.viewAddresses(); break;
      case 'payments':       this.viewPaymentMethods(); break;
      case 'notifications':  this.viewNotifications(); break;
      case 'changePassword': this.changePassword(); break;
      case 'logout':         this.logout(); break;
    }
    this.isMenuOpen = false;
  }

  // Cart methods
  toggleCartDropdown(): void {
    if (!this.isLoggedIn) {
      this.navigateToLogin();
      return;
    }
    this.isCartDropdownOpen = !this.isCartDropdownOpen;
  }

  navigateToCart(): void {
    this.router.navigate(['/cart']);
    this.isCartDropdownOpen = false;
  }

  getCartItemImageUrl(item: CartItemResponse): string {
    if (!item.productImageUrl) {
      return 'https://via.placeholder.com/60x60/007bff/ffffff?text=No+Image';
    }
    return `${environment.assetBase}${item.productImageUrl.startsWith('/') ? '' : '/'}${item.productImageUrl}`;
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount);
  }

  getCartTotal(): number {
    if (!this.cart || !this.cart.items) return 0;
    return this.cart.items.reduce((total, item) => total + item.subtotal, 0);
  }
}