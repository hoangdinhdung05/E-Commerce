import { CartItemResponse } from './CartItemResponse';

export interface CartResponse {
  id: number;
  userId: number;
  items: CartItemResponse[];
  totalItems: number;
  totalAmount: number; // Changed from totalPrice to match BE
  createdAt?: string;
  updatedAt?: string;
}
