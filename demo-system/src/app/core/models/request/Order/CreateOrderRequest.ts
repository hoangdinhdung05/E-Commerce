import { PaymentMethod } from "src/app/utils/PaymentMethod";
import { OrderItemRequest } from "./OrderItemRequest";

export interface CreateOrderRequest {
  shippingAddress: string;
  phoneNumber: string;
  receiverName: string;
  paymentMethod: PaymentMethod;
  note?: string;
  items: OrderItemRequest[];
}
