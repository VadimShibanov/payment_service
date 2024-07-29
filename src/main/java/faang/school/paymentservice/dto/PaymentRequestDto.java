package faang.school.paymentservice.dto;

import faang.school.paymentservice.model.Currency;
import faang.school.paymentservice.model.OperationStatus;
import faang.school.paymentservice.model.OperationType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestDto {
    @NotNull
    private UUID paymentId;

    @NotBlank
    private String senderAccountNumber;

    @NotBlank
    private String receiverAccountNumber;

    @Min(value = 1, message = "The minimal amount for a payment operation is 1 unit of currency.")
    @NotNull
    private BigDecimal amount;

    @NotNull
    private Currency currency;

    @NotNull
    private OperationType type;

    private OperationStatus status;
}