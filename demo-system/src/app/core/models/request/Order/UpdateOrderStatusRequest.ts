import { OrderStatus } from "src/app/utils/OrderStatus";

export interface UpdateOrderStatusRequest {
  status: OrderStatus;
}
