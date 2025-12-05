import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { CategoryRequest } from 'src/app/core/models/request/Category/CategoryRequest';
import { CategoryService } from 'src/app/core/services/categories/category.service';

declare var bootstrap: any;

@Component({
  selector: 'app-category',
  templateUrl: './category.component.html',
  styleUrls: ['./category.component.css']
})
export class CategoryComponent implements OnInit {

  categories: any[] = [];
  filteredCategories: any[] = [];
  totalPages = 0;
  totalElements = 0;
  currentPage = 0;
  pageSize = 5;
  loading = false;

  backendErrors: { [key: string]: string } = {};

  @ViewChild('createCategoryModal') createCategoryModal!: ElementRef;
  @ViewChild('editCategoryModal') editCategoryModal!: ElementRef;
  @ViewChild('detailsCategoryModal') detailsCategoryModal!: ElementRef;
  @ViewChild('deleteCategoryModal') deleteCategoryModal!: ElementRef;

  createCategoryForm!: FormGroup;
  editCategoryForm!: FormGroup;
  modalInstance: any;
  editModalInstance: any;
  detailsModalInstance: any;
  deleteModalInstance: any;
  selectedCategory: any = null;
  selectedCategoryDetails: any = null;
  categoryToDelete: any = null;

  searchText = '';



  constructor(private categoryService: CategoryService, 
    private toastr: ToastrService, 
    private fb: FormBuilder) { }

  ngOnInit(): void {
    this.createCategoryForm = this.fb.group({
      name: ['', [Validators.required]],
      description: ['']
    });

    this.editCategoryForm = this.fb.group({
      name: ['', [Validators.required]],
      description: ['']
    });

    this.loadCategories(this.currentPage > 0 && this.filteredCategories.length === 1
      ? this.currentPage - 1
      : this.currentPage);
  }

  loadCategories(page: number) {
    this.loading = true;
    this.categoryService.getAllCategories(page, this.pageSize).subscribe({
      next: (response) => {
        if (response.success) {
          this.categories = response.data.content;
          this.totalPages = response.data.totalPages;
          this.totalElements = response.data.totalElements;
          this.currentPage = response.data.pageNumber;
          this.filteredCategories = this.categories;
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading categories', err);
        this.loading = false;
      }
    });
  };
  
  nextPage() { if (this.currentPage < this.totalPages - 1) this.loadCategories(this.currentPage + 1); }
  previousPage() { if (this.currentPage > 0) this.loadCategories(this.currentPage - 1); }
  goToPage(page: number) { 
    this.loadCategories(page); 
  }

  getPageNumbers(): number[] {
    const maxPagesToShow = 5;
    const pages: number[] = [];
    
    if (this.totalPages <= maxPagesToShow) {
      // Nếu tổng số trang <= 5, hiển thị tất cả
      for (let i = 0; i < this.totalPages; i++) {
        pages.push(i);
      }
    } else {
      // Luôn hiển thị trang đầu
      pages.push(0);
      
      let startPage = Math.max(1, this.currentPage - 1);
      let endPage = Math.min(this.totalPages - 2, this.currentPage + 1);
      
      // Điều chỉnh nếu gần đầu hoặc cuối
      if (this.currentPage <= 2) {
        endPage = Math.min(3, this.totalPages - 2);
      }
      if (this.currentPage >= this.totalPages - 3) {
        startPage = Math.max(1, this.totalPages - 4);
      }
      
      // Thêm ellipsis nếu cần
      if (startPage > 1) {
        pages.push(-1); // -1 represents ellipsis
      }
      
      // Thêm các trang ở giữa
      for (let i = startPage; i <= endPage; i++) {
        pages.push(i);
      }
      
      // Thêm ellipsis nếu cần
      if (endPage < this.totalPages - 2) {
        pages.push(-1);
      }
      
      // Luôn hiển thị trang cuối
      pages.push(this.totalPages - 1);
    }
    
    return pages;
  }

  openCreateCategoryModal() {
    this.modalInstance = new bootstrap.Modal(this.createCategoryModal.nativeElement);
    this.createCategoryForm.reset();
    this.modalInstance.show();
  }

  closeCreateCategoryModal() {
    if (this.modalInstance) this.modalInstance.hide();
  }

  submitCreateCategory() {
    // Mark tất cả các field là touched để hiển thị validation
    Object.keys(this.createCategoryForm.controls).forEach(key => {
      this.createCategoryForm.get(key)?.markAsTouched();
    });

    if (this.createCategoryForm.invalid) return;

    const request: CategoryRequest = this.createCategoryForm.value;
    this.backendErrors = {}; // reset lỗi cũ

    this.categoryService.adminCreateCategory(request).subscribe({
      next: res => {
        this.toastr.success('Tạo danh mục thành công!', 'Thành công');
        this.closeCreateCategoryModal();
        this.loadCategories(this.currentPage);
      },
      error: err => {
        if (err.error) {
          // 1. Nếu backend trả về errors object
          if (err.error.errors) {
            this.backendErrors = err.error.errors;

            // Hiển thị toast chi tiết từng field
            Object.entries(err.error.errors).forEach(([field, msg]: any) => {
              this.toastr.error(msg, `Lỗi ${field}`);
            });
          } 
          // 2. Nếu backend trả về message hoặc details
          else if (err.error.message) {
            this.toastr.error(err.error.message, 'Lỗi');
          } else if (err.error.details) {
            err.error.details.forEach((d: string) => this.toastr.error(d, 'Lỗi'));
          } else {
            this.toastr.error('Có lỗi xảy ra, vui lòng thử lại!', 'Lỗi');
          }
        } else {
          this.toastr.error('Có lỗi xảy ra, vui lòng thử lại!', 'Lỗi');
        }
      }
    });
  }

  // === Search client-side ===
  filterCategories() {
    const text = this.searchText.toLowerCase();
    this.filteredCategories = this.categories.filter(c =>
      c.name?.toLowerCase().includes(text) ||
      (c.description?.toLowerCase().includes(text))
    );
  }

  exportCategoryReport() {
    this.loading = true;
    this.categoryService.exportCategoryReport().subscribe({
      next: (blob) => {
        const file = new Blob([blob], { type: 'application/pdf' });
        const fileURL = URL.createObjectURL(file);
        window.open(fileURL, '_blank');
        this.toastr.success('Xuất báo cáo PDF thành công!', 'Thành công');
        this.loading = false;
      },
      error: (err) => {
        console.error('Export report error:', err);
        this.toastr.error('Không thể xuất báo cáo PDF!', 'Lỗi');
        this.loading = false;
      }
    });
  }

  openEditModal(category: any) {
    this.selectedCategory = category;
    this.editCategoryForm.patchValue({
      name: category.name,
      description: category.description
    });

    this.editModalInstance = new bootstrap.Modal(this.editCategoryModal.nativeElement);
    this.editModalInstance.show();
  }

  closeEditModal() {
    if (this.editModalInstance) this.editModalInstance.hide();
  }

  submitEditCategory() {
    if (this.editCategoryForm.invalid || !this.selectedCategory) return;

    const request = this.editCategoryForm.value;
    this.categoryService.updateCategory(this.selectedCategory.id, request).subscribe({
      next: res => {
        this.toastr.success('Cập nhật danh mục thành công!', 'Thành công');
        this.closeEditModal();
        this.loadCategories(this.currentPage);
      },
      error: err => {
        console.error('Update error:', err);
        this.toastr.error(err.error?.message || 'Có lỗi xảy ra khi cập nhật!', 'Lỗi');
      }
    });
  }

  openDeleteModal(category: any) {
    this.categoryToDelete = category;
    this.deleteModalInstance = new bootstrap.Modal(this.deleteCategoryModal.nativeElement);
    this.deleteModalInstance.show();
  }

  closeDeleteModal() {
    if (this.deleteModalInstance) this.deleteModalInstance.hide();
    this.categoryToDelete = null;
  }

  confirmDeleteCategory() {
    if (!this.categoryToDelete) return;

    this.categoryService.deleteCategory(this.categoryToDelete.id).subscribe({
      next: (res) => {
        if (res.success) {
          this.toastr.success('Xóa danh mục thành công!', 'Thành công');
          this.closeDeleteModal();
          this.loadCategories(this.currentPage);
        } else {
          this.toastr.warning('Không thể xóa danh mục này!', 'Cảnh báo');
        }
      },
      error: (err) => {
        console.error('Error deleting category:', err);
        if (err.error?.message) {
          this.toastr.error(err.error.message, 'Lỗi');
        } else {
          this.toastr.error('Có lỗi xảy ra, vui lòng thử lại!', 'Lỗi');
        }
        this.closeDeleteModal();
      }
    });
  }

  closeDetailsModal() {
    if (this.detailsModalInstance) this.detailsModalInstance.hide();
  }

}
