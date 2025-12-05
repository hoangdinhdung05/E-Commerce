import { PaymentMethod } from "src/app/utils/PaymentMethod";

export interface CreatePaymentRequest {
  orderId: number;
  paymentMethod: PaymentMethod;
  paymentInfo?: string;
}
