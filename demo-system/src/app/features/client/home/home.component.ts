import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FilterOptions } from '../components/product-filter/product-filter.component';

interface FeaturedCategory {
  id: number;
  name: string;
  icon: string;
  productCount: number;
}

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent {
  currentFilters: FilterOptions | null = null;

  // Featured categories (mock data for now)
  featuredCategories: FeaturedCategory[] = [
    { id: 1, name: 'Điện tử', icon: 'fas fa-laptop', productCount: 120 },
    { id: 2, name: 'Thời trang', icon: 'fas fa-tshirt', productCount: 85 },
    { id: 3, name: 'Gia dụng', icon: 'fas fa-home', productCount: 67 },
    { id: 4, name: 'Thể thao', icon: 'fas fa-running', productCount: 45 },
    { id: 5, name: 'Sách & Văn phòng phẩm', icon: 'fas fa-book', productCount: 92 },
    { id: 6, name: 'Sức khỏe & Làm đẹp', icon: 'fas fa-heart', productCount: 73 }
  ];

  constructor(private router: Router) {}

  onFilterChange(filters: FilterOptions): void {
    this.currentFilters = filters;
  }

  exploreProducts(): void {
    // Scroll to products section or navigate to all products
    const productSection = document.querySelector('.product-section');
    if (productSection) {
      productSection.scrollIntoView({ behavior: 'smooth' });
    }
  }

  navigateToCategory(categoryId: number): void {
    this.router.navigate(['/client/category', categoryId]);
  }
}
