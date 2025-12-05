import { OrderStatus } from "src/app/utils/OrderStatus";
import { PaymentMethod } from "src/app/utils/PaymentMethod";
import { PaymentStatus } from "src/app/utils/PaymentStatus";
import { OrderItemResponse } from "./OrderItemResponse";

export interface OrderResponse {
  id: number;
  orderNumber: string;
  userId: number;
  username?: string;
  totalAmount: number;
  shippingAddress: string;
  phoneNumber: string;
  receiverName: string;
  status: OrderStatus;
  paymentStatus: PaymentStatus;
  paymentMethod: PaymentMethod;
  note?: string;
  items: OrderItemResponse[];
  createdAt: string;
  updatedAt: string;
}
