import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { ProductService } from 'src/app/core/services/products/product.service';
import { CategoryService } from 'src/app/core/services/categories/category.service';
import { UploadService } from 'src/app/core/services/upload/upload.service';
import { environment } from 'src/environments/environment';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

declare var bootstrap: any;

@Component({
  selector: 'app-product',
  templateUrl: './product.component.html',
  styleUrls: ['./product.component.css']
})
export class ProductComponent implements OnInit {

  products: any[] = [];
  filteredProducts: any[] = [];
  totalPages = 0;
  totalElements = 0;
  currentPage = 0;
  pageSize = 10;
  loading = false;

  @ViewChild('createProductModal') createProductModal!: ElementRef;
  @ViewChild('editProductModal') editProductModal!: ElementRef;
  @ViewChild('detailsProductModal') detailsProductModal!: ElementRef;
  @ViewChild('deleteProductModal') deleteProductModal!: ElementRef;

  createProductForm!: FormGroup;
  editProductForm!: FormGroup;
  modalInstance: any;
  editModalInstance: any;
  detailsModalInstance: any;
  deleteModalInstance: any;
  selectedProduct: any = null;
  selectedProductDetails: any = null;
  productToDelete: any = null;

  searchText = '';
  imageFile?: File;
  imagePreview?: string; // Thêm để preview ảnh
  editImageFile?: File; // File ảnh mới cho edit
  editImagePreview?: string; // Preview ảnh mới cho edit
  categoryOptions: any[] = [];
  
  private searchSubject = new Subject<string>();

  constructor(
    private productService: ProductService, 
    private toastr: ToastrService, 
    private fb: FormBuilder,
    private categoryService: CategoryService,
    private uploadService: UploadService
  ) { }

  imageUrl(product: any) {
    return `${environment.assetBase}${product.productImageUrl}`;
  }

  ngOnInit(): void {
    this.createProductForm = this.fb.group({
      name: ['', [Validators.required]],
      description: [''],
      price: [0, [Validators.required, Validators.min(0)]],
      categoryId: [null],
      quantity: [0, [Validators.required, Validators.min(0)]]
    });

    this.editProductForm = this.fb.group({
      name: ['', [Validators.required]],
      description: [''],
      price: [0, [Validators.required, Validators.min(0)]],
      categoryId: [null],
      quantity: [0, [Validators.required, Validators.min(0)]]
    });

    this.loadProducts(this.currentPage);
    this.loadCategoryOptions();
    
    // Setup debounced search
    this.searchSubject.pipe(
      debounceTime(400), // Chờ 400ms sau khi người dùng ngừng gõ
      distinctUntilChanged() // Chỉ gọi API khi giá trị thay đổi
    ).subscribe(searchText => {
      this.performSearch(searchText);
    });
  }

  loadCategoryOptions() {
    // load many categories (page 0, large pageSize) to fill select options
    this.categoryService.getAllCategories(0, 1000).subscribe({
      next: (res) => {
        if (res.success) {
          this.categoryOptions = res.data.content || [];
        }
      },
      error: (err) => {
        console.error('Error loading category options', err);
      }
    });
  }

  loadProducts(page: number) {
    this.loading = true;
    this.productService.getAllProducts(page, this.pageSize).subscribe({
      next: (response) => {
        if (response.success) {
          this.products = response.data.content;
          this.totalPages = response.data.totalPages;
          this.totalElements = response.data.totalElements;
          this.currentPage = response.data.pageNumber;
          this.filteredProducts = this.products;
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading products', err);
        this.toastr.error('Không thể tải danh sách sản phẩm!', 'Lỗi');
        this.loading = false;
      }
    });
  }

  nextPage() { 
    if (this.currentPage < this.totalPages - 1) this.loadProducts(this.currentPage + 1); 
  }
  
  previousPage() { 
    if (this.currentPage > 0) this.loadProducts(this.currentPage - 1); 
  }
  
  goToPage(page: number) { 
    this.loadProducts(page); 
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

  filterProducts() {
    this.searchSubject.next(this.searchText);
  }

  private performSearch(text: string) {
    const searchText = text.trim();
    
    // Nếu không có text, load lại danh sách phân trang
    if (!searchText) {
      this.loadProducts(this.currentPage);
      return;
    }
    
    // Gọi API search theo tên
    this.loading = true;
    this.productService.searchByName(searchText).subscribe({
      next: (response) => {
        if (response.success) {
          this.filteredProducts = response.data;
          
          // Reset pagination vì search không có phân trang
          this.totalPages = this.filteredProducts.length > 0 ? 1 : 0;
          this.totalElements = this.filteredProducts.length;
          this.currentPage = 0;
          
          if (this.filteredProducts.length > 0) {
            this.toastr.info(`Tìm thấy ${this.filteredProducts.length} sản phẩm`, 'Kết quả');
          } else {
            this.toastr.warning('Không tìm thấy sản phẩm nào!', 'Thông báo');
          }
        } else {
          this.filteredProducts = [];
          this.totalPages = 0;
          this.totalElements = 0;
          this.currentPage = 0;
          this.toastr.warning('Không tìm thấy sản phẩm nào!', 'Thông báo');
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Search error:', err);
        this.toastr.error('Có lỗi xảy ra khi tìm kiếm!', 'Lỗi');
        this.filteredProducts = [];
        this.totalPages = 0;
        this.totalElements = 0;
        this.currentPage = 0;
        this.loading = false;
      }
    });
  }

  resetSearch() {
    this.searchText = '';
    this.loadProducts(this.currentPage);
  }

  exportProductReport() {
    this.loading = true;
    
    // Lấy tên sản phẩm từ ô tìm kiếm (nếu có)
    const productName = this.searchText.trim() || undefined;
    
    this.productService.exportProductReport(productName).subscribe({
      next: (blob) => {
        const file = new Blob([blob], { type: 'application/pdf' });
        const fileURL = URL.createObjectURL(file);
        window.open(fileURL, '_blank');
        
        if (productName) {
          this.toastr.success(`Xuất báo cáo PDF cho sản phẩm "${productName}" thành công!`, 'Thành công');
        } else {
          this.toastr.success('Xuất báo cáo PDF tất cả sản phẩm thành công!', 'Thành công');
        }
        
        this.loading = false;
      },
      error: (err) => {
        console.error('Export report error:', err);
        this.toastr.error('Không thể xuất báo cáo PDF!', 'Lỗi');
        this.loading = false;
      }
    });
  }

  // === CREATE PRODUCT ===
  openCreateModal() {
    this.modalInstance = new bootstrap.Modal(this.createProductModal.nativeElement);
    this.createProductForm.reset({ price: 0 });
    this.imageFile = undefined;
    this.imagePreview = undefined;
    this.modalInstance.show();
  }

  closeCreateModal() {
    if (this.modalInstance) this.modalInstance.hide();
  }

  onImageSelected(event: any) {
    const file: File = event.target.files?.[0];
    if (!file) { this.imageFile = undefined; this.imagePreview = undefined; return; }

    const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];
    if (!validTypes.includes(file.type)) {
      this.toastr.error('Chỉ chấp nhận file ảnh (JPG, PNG, WEBP)!', 'Lỗi');
      event.target.value = '';
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      this.toastr.error('Kích thước file không được vượt quá 5MB!', 'Lỗi');
      event.target.value = '';
      return;
    }

    this.imageFile = file;

    const reader = new FileReader();
    reader.onload = (e: any) => this.imagePreview = e.target.result;
    reader.readAsDataURL(file);
  }

  onEditImageSelected(event: any) {
    const file: File = event.target.files?.[0];
    if (!file) { 
      this.editImageFile = undefined; 
      this.editImagePreview = undefined; 
      return; 
    }

    const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];
    if (!validTypes.includes(file.type)) {
      this.toastr.error('Chỉ chấp nhận file ảnh (JPG, PNG, WEBP)!', 'Lỗi');
      event.target.value = '';
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      this.toastr.error('Kích thước file không được vượt quá 5MB!', 'Lỗi');
      event.target.value = '';
      return;
    }

    this.editImageFile = file;

    const reader = new FileReader();
    reader.onload = (e: any) => this.editImagePreview = e.target.result;
    reader.readAsDataURL(file);
  }


  submitCreateProduct() {
    // Validation
    if (this.createProductForm.invalid) {
      this.toastr.warning('Vui lòng điền đầy đủ thông tin!', 'Cảnh báo');
      return;
    }

    if (!this.imageFile) {
      this.toastr.warning('Vui lòng chọn ảnh sản phẩm!', 'Cảnh báo');
      return;
    }

    this.loading = true;

    // Bước 1: Upload ảnh trước
    this.toastr.info('Đang upload ảnh...', 'Xử lý');
    this.uploadService.uploadProductImage(this.imageFile).subscribe({
      next: uploadRes => {
        console.log('Upload success:', uploadRes);
        
        // Bước 2: Tạo product với imageUrl từ upload
        this.toastr.info('Đang tạo sản phẩm...', 'Xử lý');
        const v = this.createProductForm.value;
        const request = {
          name: (v.name ?? '').trim(),
          description: v.description ?? '',
          price: v.price ?? 0,
          quantity: v.quantity ?? 0,
          categoryId: v.categoryId,
          imageUrl: uploadRes.publicUrl // Lấy publicUrl từ Upload API
        };

        console.log('Create product request:', request);

        this.productService.createProduct(request).subscribe({
          next: res => {
            if (res.success) {
              this.toastr.success('Tạo sản phẩm thành công!', 'Thành công');
              this.closeCreateModal();
              this.loadProducts(this.currentPage);
            } else {
              this.toastr.warning(res.message || 'Không thể tạo sản phẩm!', 'Cảnh báo');
            }
            this.loading = false;
          },
          error: err => {
            console.error('Create product error:', err);
            this.toastr.error(err.error?.message || 'Có lỗi xảy ra khi tạo sản phẩm!', 'Lỗi');
            this.loading = false;
          }
        });
      },
      error: err => {
        console.error('Upload image error:', err);
        this.toastr.error(err.error?.message || 'Có lỗi xảy ra khi upload ảnh!', 'Lỗi');
        this.loading = false;
      }
    });
  }


  // === EDIT PRODUCT ===
  openEditModal(product: any) {
    this.selectedProduct = product;
    
    // Debug log để kiểm tra cấu trúc dữ liệu
    console.log('Opening edit modal for product:', product);
    console.log('Category ID:', product.category?.id);
    console.log('Available category options:', this.categoryOptions);
    
    // Đảm bảo categoryId được set đúng
    const categoryId = product.category?.id || product.categoryId || null;
    
    this.editProductForm.patchValue({
      name: product.name || '',
      description: product.description || '',
      price: product.price || 0,
      quantity: product.quantity || 0,
      categoryId: categoryId
    });

    // Log giá trị sau khi patch
    console.log('Form values after patch:', this.editProductForm.value);
    console.log('Selected categoryId:', categoryId);

    // Reset edit image preview
    this.editImageFile = undefined;
    this.editImagePreview = undefined;

    // Mở modal với một chút delay để đảm bảo form đã được patch xong
    setTimeout(() => {
      this.editModalInstance = new bootstrap.Modal(this.editProductModal.nativeElement);
      this.editModalInstance.show();
    }, 50);
  }

  closeEditModal() {
    if (this.editModalInstance) this.editModalInstance.hide();
  }

  submitEditProduct() {
    if (this.editProductForm.invalid || !this.selectedProduct) return;

    this.loading = true;

    // Nếu có chọn ảnh mới, upload ảnh trước
    if (this.editImageFile) {
      this.toastr.info('Đang upload ảnh mới...', 'Xử lý');
      this.uploadService.uploadProductImage(this.editImageFile).subscribe({
        next: uploadRes => {
          console.log('Upload new image success:', uploadRes);
          
          // Cập nhật product với imageUrl mới
          const request = {
            ...this.editProductForm.value,
            imageUrl: uploadRes.publicUrl
          };
          
          this.updateProductData(request);
        },
        error: err => {
          console.error('Upload image error:', err);
          this.toastr.error(err.error?.message || 'Có lỗi xảy ra khi upload ảnh!', 'Lỗi');
          this.loading = false;
        }
      });
    } else {
      // Không có ảnh mới, chỉ cập nhật thông tin
      const request = this.editProductForm.value;
      this.updateProductData(request);
    }
  }

  private updateProductData(request: any) {
    this.productService.updateProduct(this.selectedProduct.id, request).subscribe({
      next: res => {
        this.toastr.success('Cập nhật sản phẩm thành công!', 'Thành công');
        this.closeEditModal();
        this.loadProducts(this.currentPage);
        this.loading = false;
      },
      error: err => {
        console.error('Update error:', err);
        this.toastr.error(err.error?.message || 'Có lỗi xảy ra khi cập nhật!', 'Lỗi');
        this.loading = false;
      }
    });
  }

  // === DELETE PRODUCT ===
  openDeleteModal(product: any) {
    this.productToDelete = product;
    this.deleteModalInstance = new bootstrap.Modal(this.deleteProductModal.nativeElement);
    this.deleteModalInstance.show();
  }

  closeDeleteModal() {
    if (this.deleteModalInstance) this.deleteModalInstance.hide();
    this.productToDelete = null;
  }

  confirmDeleteProduct() {
    if (!this.productToDelete) return;

    this.productService.deleteProduct(this.productToDelete.id).subscribe({
      next: res => {
        if (res.success) {
          this.toastr.success('Xóa sản phẩm thành công!', 'Thành công');
          this.closeDeleteModal();
          this.loadProducts(
            this.currentPage > 0 && this.filteredProducts.length === 1
              ? this.currentPage - 1
              : this.currentPage
          );
        } else {
          this.toastr.warning('Không thể xóa sản phẩm này!', 'Cảnh báo');
        }
      },
      error: err => {
        console.error('Error deleting product:', err);
        this.toastr.error(err.error?.message || 'Có lỗi xảy ra!', 'Lỗi');
        this.closeDeleteModal();
      }
    });
  }

  // === DETAILS PRODUCT ===
  openDetailsModal(productId: number) {
    this.productService.getProductDetails(productId).subscribe({
      next: res => {
        if (res.success) {
          this.selectedProductDetails = res.data;
          this.detailsModalInstance = new bootstrap.Modal(this.detailsProductModal.nativeElement);
          this.detailsModalInstance.show();
        } else {
          this.toastr.warning('Không tìm thấy thông tin sản phẩm', 'Cảnh báo');
        }
      },
      error: err => {
        console.error('Lỗi khi lấy chi tiết product:', err);
        this.toastr.error('Không thể tải thông tin chi tiết!', 'Lỗi');
      }
    });
  }

  closeDetailsModal() {
    if (this.detailsModalInstance) this.detailsModalInstance.hide();
  }

}