package faang.school.paymentservice.service;

import faang.school.paymentservice.client.AccountServiceClient;
import faang.school.paymentservice.dto.PaymentRequestDto;
import faang.school.paymentservice.dto.PaymentResponseDto;
import faang.school.paymentservice.mapper.PaymentRequestMapper;
import faang.school.paymentservice.model.OperationStatus;
import faang.school.paymentservice.model.PaymentRequest;
import faang.school.paymentservice.redis.PaymentEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Slf4j
@Setter
@Service
@RequiredArgsConstructor
public class AccountServiceConnector {
    private final AccountServiceClient accountServiceClient;
    private final PaymentEventPublisher paymentEventPublisher;
    private final PaymentRequestMapper mapper;
    @Value("${account-service.repeatable-delay}")
    private final int backoffDelay = 3000;


    @Retryable(recover = "handleFailedAuthorization", backoff = @Backoff(delay = backoffDelay))
    public PaymentResponseDto authorizePayment(PaymentRequestDto paymentRequestDto) {
        log.info("Sending authorization message to account-service");
        return accountServiceClient.authorizePayment(paymentRequestDto);
    }

    @Recover
    public PaymentResponseDto handleFailedAuthorization(PaymentRequestDto paymentRequestDto) {
        log.warn("Connection to account-service failed");
        PaymentResponseDto responseDto = mapper.toPaymentResponse(paymentRequestDto);
        responseDto.setStatus(OperationStatus.FAILED);
        responseDto.setMessage("Server exception. Please try again later.");

        return responseDto;
    }

    public void publishClearingEvent(PaymentRequest pendingRequest) {
        log.info("Sending clearing message to account-service");
        paymentEventPublisher.publish(mapper.toDto(pendingRequest));
    }
}
