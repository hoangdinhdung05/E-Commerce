import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { takeUntil } from 'rxjs/operators';
import { CategoryService } from '../../../../core/services/categories/category.service';
import { CategoryResponse } from '../../../../core/models/response/Category/CategoryResponse';
import { ProductService } from 'src/app/core/services/products/product.service';
import { forkJoin } from 'rxjs';
import { DestroyableComponent } from '../../../../shared/components/base/destroyable.component';

export interface FilterOptions {
  categories: number[];
  priceRange: { min: number; max: number };
  sortBy: string;
  inStock: boolean;
}

interface CategoryWithCount extends CategoryResponse {
  count?: number;
}

@Component({
  selector: 'app-product-filter',
  templateUrl: './product-filter.component.html',
  styleUrls: ['./product-filter.component.css']
})
export class ProductFilterComponent extends DestroyableComponent implements OnInit {
  @Output() filterChange = new EventEmitter<FilterOptions>();

  categories: CategoryWithCount[] = [];
  isLoadingCategories = false;

  selectedCategory: number | null = null;
  priceRange = { min: 0, max: 10000 };
  sortBy = 'name';
  inStock = false;
  countProductByCategory: number | null = null;

  sortOptions = [
    { value: 'name', label: 'Tên A-Z' },
    { value: 'name-desc', label: 'Tên Z-A' },
    { value: 'price', label: 'Giá thấp đến cao' },
    { value: 'price-desc', label: 'Giá cao đến thấp' },
    { value: 'newest', label: 'Mới nhất' }
  ];

  constructor(
    private categoryService: CategoryService,
    private productService: ProductService
  ) {
    super();
  }

  ngOnInit(): void {
    this.loadCategories();
    this.emitFilterChange();
  }

  loadCategories(): void {
    this.isLoadingCategories = true;
    console.log('Loading categories for filter...');
    
    this.categoryService.getAll(0, 10)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
      next: (response) => {
        console.log('Category service response:', response);
        if (response.success && response.data) {
          this.categories = response.data.content.map(category => ({
            ...category,
            

          }));
          console.log('Categories loaded 111:', this.categories);
          for (let i = 0; i < this.categories.length; i++) {
            console.log('1111111 Category ', i, ': ', this.categories[i]);
          }
        } else {
          console.error('Category API response not successful or no data:', response);
        }
        this.isLoadingCategories = false;
      },
      error: (error) => {
        console.error('Error loading categories:', error);
        this.isLoadingCategories = false;
      }
    });
  }

  // loadCategories(): void {
  //   this.isLoadingCategories = true;
  //   console.log('Loading categories for filter...');

  //   this.categoryService.getAllCategories(0, 10).subscribe({
  //     next: (response) => {
  //       if (!response.success || !response.data) {
  //         console.error('Category API response not successful:', response);
  //         this.isLoadingCategories = false;
  //         return;
  //       }

  //       const rawCategories = response.data.content;

  //       // Tạo các request count
  //       const countRequests = rawCategories.map(c =>
  //         this.productService.countByCategoryId(c.id)
  //       );

  //       forkJoin(countRequests).subscribe({
  //         next: (counts) => {
  //           // Gắn count theo index
  //           this.categories = rawCategories.map((cat, index) => ({
  //             ...cat,
  //             count: counts[index]
  //           }));

  //           console.log('Categories with count:', this.categories);
  //           this.isLoadingCategories = false;
  //         },

  //         error: (err) => {
  //           console.error("Error loading category counts:", err);
  //           this.isLoadingCategories = false;
  //         }
  //       });
  //     },

  //     error: (err) => {
  //       console.error('Error loading categories:', err);
  //       this.isLoadingCategories = false;
  //     }
  //   });
  // }


  toggleCategory(categoryId: number): void {
    if (this.selectedCategory === categoryId) {
      this.selectedCategory = null;
    } else {
      this.selectedCategory = categoryId;
    }
    this.emitFilterChange();
  }

  onPriceRangeChange(): void {
    this.emitFilterChange();
  }

  onSortChange(): void {
    this.emitFilterChange();
  }

  onStockFilterChange(): void {
    this.emitFilterChange();
  }

  clearFilters(): void {
    this.selectedCategory = null;
    this.priceRange = { min: 0, max: 10000 };
    this.sortBy = 'name';
    this.inStock = false;
    this.emitFilterChange();
  }

  private emitFilterChange(): void {
    const filters: FilterOptions = {
      categories: this.selectedCategory !== null ? [this.selectedCategory] : [],
      priceRange: this.priceRange,
      sortBy: this.sortBy,
      inStock: this.inStock
    };
    this.filterChange.emit(filters);
    console.log("Category 2222: ", filters.categories);
  }
}