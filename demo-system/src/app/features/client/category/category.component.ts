import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FilterOptions } from '../components/product-filter/product-filter.component';

@Component({
  selector: 'app-category',
  templateUrl: './category.component.html',
  styleUrls: ['./category.component.css']
})
export class CategoryComponent implements OnInit {
  currentFilters: FilterOptions | null = null;
  categoryId: number | null = null;
  categoryName: string = '';

  constructor(private route: ActivatedRoute) { }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const categoryParam = params['category'];
      if (categoryParam) {
        // Try to parse as number (ID), if that fails treat as name
        const parsedId = parseInt(categoryParam);
        if (!isNaN(parsedId)) {
          this.categoryId = parsedId;
          // Pre-filter by category ID
          this.currentFilters = {
            categories: [this.categoryId],
            priceRange: { min: 0, max: 1000 },
            sortBy: 'name',
            inStock: false
          };
        } else {
          this.categoryName = categoryParam;
          // You might want to fetch category ID by name from API
        }
      }
    });
  }

  onFilterChange(filters: FilterOptions): void {
    this.currentFilters = filters;
  }
}