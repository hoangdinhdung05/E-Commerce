import { ProductResponse } from '../Product/ProductResponse';

export interface CartItemResponse {
  id: number;
  productId: number;
  product?: ProductResponse; // Optional since BE might not always include full product
  productName: string;
  quantity: number;
  price: number;
  subtotal: number;
  productImageUrl?: string;
  availableStock: number;
}
