export enum PaymentMethod {
  COD = 'COD',                    // Cash on Delivery
  BANK_TRANSFER = 'BANK_TRANSFER', // Chuyển khoản ngân hàng
  VNPAY = 'VNPAY',                // VNPay gateway
  MOMO = 'MOMO',                  // MoMo gateway
  CREDIT_CARD = 'CREDIT_CARD'     // Thẻ tín dụng
}

export const PaymentMethodLabels: { [key in PaymentMethod]: string } = {
  [PaymentMethod.COD]: 'Thanh toán khi nhận hàng',
  [PaymentMethod.BANK_TRANSFER]: 'Chuyển khoản ngân hàng',
  [PaymentMethod.VNPAY]: 'VNPay',
  [PaymentMethod.MOMO]: 'MoMo',
  [PaymentMethod.CREDIT_CARD]: 'Thẻ tín dụng'
};
