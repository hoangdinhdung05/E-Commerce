import { PaymentStatus } from '../../../../utils/PaymentStatus';

export interface ConfirmPaymentRequest {
  status: PaymentStatus; // PAID, FAILED, REFUNDED
  transactionId?: string; // Optional: Transaction ID from gateway
  paymentInfo?: string;
  errorMessage?: string; // Error message if payment failed
}
