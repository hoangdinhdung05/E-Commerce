import { PaymentMethod } from "src/app/utils/PaymentMethod";
import { PaymentStatus } from "src/app/utils/PaymentStatus";

export interface PaymentResponse {
  id: number;
  orderId: number;
  orderNumber: string;
  paymentMethod: PaymentMethod;
  amount: number;
  transactionId?: string;
  status: PaymentStatus;
  paymentDate?: string;
  paymentInfo?: string;
  errorMessage?: string;
  createdAt: string;
}
