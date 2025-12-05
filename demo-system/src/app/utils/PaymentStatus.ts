export enum PaymentStatus {
  UNPAID = 'UNPAID',      // Chưa thanh toán
  PENDING = 'PENDING',    // Đang chờ xử lý
  PAID = 'PAID',          // Đã thanh toán
  FAILED = 'FAILED',      // Thanh toán thất bại
  REFUNDED = 'REFUNDED'   // Đã hoàn tiền
}

export const PaymentStatusLabels: { [key in PaymentStatus]: string } = {
  [PaymentStatus.UNPAID]: 'Chưa thanh toán',
  [PaymentStatus.PENDING]: 'Đang chờ xử lý',
  [PaymentStatus.PAID]: 'Đã thanh toán',
  [PaymentStatus.FAILED]: 'Thanh toán thất bại',
  [PaymentStatus.REFUNDED]: 'Đã hoàn tiền'
};

export const PaymentStatusColors: { [key in PaymentStatus]: string } = {
  [PaymentStatus.UNPAID]: 'warning',
  [PaymentStatus.PENDING]: 'info',
  [PaymentStatus.PAID]: 'success',
  [PaymentStatus.FAILED]: 'danger',
  [PaymentStatus.REFUNDED]: 'secondary'
};
