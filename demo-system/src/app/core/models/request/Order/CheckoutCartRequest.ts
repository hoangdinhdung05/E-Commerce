import { PaymentMethod } from "src/app/utils/PaymentMethod";

export interface CheckoutCartRequest {
  shippingAddress: string;
  phoneNumber: string;
  receiverName: string;
  paymentMethod: PaymentMethod;
  note?: string;
}
