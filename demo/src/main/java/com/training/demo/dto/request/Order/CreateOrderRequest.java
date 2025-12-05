package com.training.demo.dto.request.Order;

import com.training.demo.utils.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotBlank(message = "Shipping address is required")
    @Size(min = 10, max = 500, message = "Shipping address must be between 10 and 500 characters")
    private String shippingAddress;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^(0|\\+84)[3|5|7|8|9][0-9]{8}$",
            message = "Invalid Vietnamese phone number format")
    private String phoneNumber;

    @NotBlank(message = "Receiver name is required")
    @Size(min = 2, max = 200, message = "Receiver name must be between 2 and 200 characters")
    @Pattern(regexp = "^[\\p{L}\\s]+$", message = "Receiver name must contain only letters and spaces")
    private String receiverName;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @Size(max = 1000, message = "Note must not exceed 1000 characters")
    private String note;

    @NotEmpty(message = "Order items cannot be empty")
    @Size(min = 1, max = 50, message = "Order must contain between 1 and 50 items")
    @Valid // ✅ Important: validate nested objects
    private List<OrderItemRequest> items;
}