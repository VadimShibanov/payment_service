package faang.school.paymentservice.client;

import faang.school.paymentservice.dto.PaymentRequestDto;
import faang.school.paymentservice.dto.PaymentResponseDto;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "account-service", url = "${account-service.host}:${account-service.port}")
public interface AccountServiceClient {

    @PostMapping("/accounts/payments/authorization")
    PaymentResponseDto authorizePayment(@RequestBody @Valid PaymentRequestDto paymentRequestDto);
}
