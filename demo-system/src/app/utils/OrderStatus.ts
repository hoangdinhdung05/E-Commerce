export enum OrderStatus {
  PENDING = 'PENDING',        // Chờ xác nhận
  CONFIRMED = 'CONFIRMED',    // Đã xác nhận
  PROCESSING = 'PROCESSING',  // Đang xử lý
  SHIPPING = 'SHIPPING',      // Đang giao hàng
  DELIVERED = 'DELIVERED',    // Đã giao hàng
  CANCELLED = 'CANCELLED',    // Đã hủy
  REFUNDED = 'REFUNDED'       // Đã hoàn tiền
}

export const OrderStatusLabels: { [key in OrderStatus]: string } = {
  [OrderStatus.PENDING]: 'Chờ xác nhận',
  [OrderStatus.CONFIRMED]: 'Đã xác nhận',
  [OrderStatus.PROCESSING]: 'Đang xử lý',
  [OrderStatus.SHIPPING]: 'Đang giao hàng',
  [OrderStatus.DELIVERED]: 'Đã giao hàng',
  [OrderStatus.CANCELLED]: 'Đã hủy',
  [OrderStatus.REFUNDED]: 'Đã hoàn tiền'
};

export const OrderStatusColors: { [key in OrderStatus]: string } = {
  [OrderStatus.PENDING]: 'warning',
  [OrderStatus.CONFIRMED]: 'info',
  [OrderStatus.PROCESSING]: 'primary',
  [OrderStatus.SHIPPING]: 'primary',
  [OrderStatus.DELIVERED]: 'success',
  [OrderStatus.CANCELLED]: 'danger',
  [OrderStatus.REFUNDED]: 'secondary'
};
