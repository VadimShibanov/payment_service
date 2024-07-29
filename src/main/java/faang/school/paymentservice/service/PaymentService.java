package faang.school.paymentservice.service;

import faang.school.paymentservice.dto.PaymentRequestDto;
import faang.school.paymentservice.dto.PaymentResponseDto;
import faang.school.paymentservice.exception.DataValidationException;
import faang.school.paymentservice.mapper.PaymentRequestMapper;
import faang.school.paymentservice.model.OperationStatus;
import faang.school.paymentservice.model.PaymentRequest;
import faang.school.paymentservice.redis.PaymentEventPublisher;
import faang.school.paymentservice.repository.PaymentRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private static final TemporalUnit CLEARING_DELAY_TEMPORAL_UNIT = ChronoUnit.MINUTES;
    @Value("${clear-scheduler.auto-clear-delay-amount}")
    private long clearingDelay;
    private final PaymentRequestRepository repository;
    private final PaymentRequestMapper mapper;
    private final PaymentEventPublisher eventPublisher;
    private final PaymentServiceValidator verifier;
    private final AccountServiceConnector accountServiceConnector;


    @Transactional
    public PaymentResponseDto sendPayment(PaymentRequestDto dto) {
        if (repository.existsByIdAndStatusIsNotFailed(dto.getPaymentId())) {
            throw new DataValidationException("This payment has already been registered for authorization");
        }

        PaymentRequest pendingRequest = repository.save(createPendingRequest(dto));
        log.info("Saved new pending operation with id = {}", dto.getPaymentId());

        PaymentResponseDto responseDto = accountServiceConnector.authorizePayment(dto);
        pendingRequest.setStatus(responseDto.getStatus());
        log.info("Updated status of pending operation with id = {} to status = {}", dto.getPaymentId(), responseDto.getStatus());

        return responseDto;
    }


    @Transactional
    public PaymentResponseDto cancelPayment(UUID paymentId) {
        PaymentRequest pendingRequestForClearing = getPendingRequestForClearing(paymentId);

        log.info("Start canceling operation with id = {}", paymentId);
        clearRequest(pendingRequestForClearing, OperationStatus.CANCELING);

        return formCancelingResponse(pendingRequestForClearing);
    }

    @Transactional
    public PaymentResponseDto confirmPayment(UUID paymentId, BigDecimal newAmount) {
        PaymentRequest pendingRequestForClearing = getPendingRequestForClearing(paymentId);

        if (newAmount != null) {
            pendingRequestForClearing.setAmount(newAmount);
        }

        log.info("Start confirming operation with id = {}", paymentId);
        clearRequest(pendingRequestForClearing, OperationStatus.CONFIRMATION);

        return formConfirmationResponse(pendingRequestForClearing);
    }

    @Scheduled(cron = "${clear-scheduler.cron}")
    public void autoClearRequest() {
        log.info("Selecting first 5 requests to be cleared.");
        List<PaymentRequest> pendingRequests = repository.findReadyToClearRequests();

        log.info("Clearing {} requests.", pendingRequests.size());
        pendingRequests.forEach(pendingRequest -> clearRequest(pendingRequest, OperationStatus.CONFIRMATION));
    }

    @Async
    @Transactional
    public void clearRequest(PaymentRequest pendingRequest, OperationStatus operationStatus) {
        pendingRequest.setStatus(operationStatus);
        repository.save(pendingRequest);

        accountServiceConnector.publishClearingEvent(pendingRequest);

        pendingRequest.setStatus(OperationStatus.CLEARING);
        repository.save(pendingRequest);
    }

    private PaymentRequest getPendingRequestForClearing(UUID paymentId) {
        PaymentRequest pendingRequest = repository.findById(paymentId);

        verifier.validateRequestBeforeClearing(pendingRequest);
        return pendingRequest;
    }

    private PaymentResponseDto formCancelingResponse(PaymentRequest pendingRequest) {
        return formResponse(
                pendingRequest,
                OperationStatus.CANCELED,
                "Your payment has been canceled.");
    }

    private PaymentResponseDto formConfirmationResponse(PaymentRequest pendingRequest) {
        return formResponse(
                pendingRequest,
                OperationStatus.CONFIRMED,
                "Your payment has been confirmed and succeed.");
    }

    private PaymentResponseDto formResponse(PaymentRequest pendingRequest, OperationStatus status, String message) {
        PaymentResponseDto response = mapper.toPaymentResponse(pendingRequest);
        response.setStatus(status);
        response.setMessage(message);

        return response;
    }

    private PaymentRequest createPendingRequest(PaymentRequestDto dto) {
        PaymentRequest pendingRequest = mapper.toModel(dto);
        pendingRequest.setStatus(OperationStatus.NEW);
        dto.setStatus(OperationStatus.NEW);
        pendingRequest.setClearScheduledAt(LocalDateTime.now().plus(clearingDelay, CLEARING_DELAY_TEMPORAL_UNIT));

        return pendingRequest;
    }
}