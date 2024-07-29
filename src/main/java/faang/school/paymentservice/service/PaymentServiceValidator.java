package faang.school.paymentservice.service;

import faang.school.paymentservice.exception.DataValidationException;
import faang.school.paymentservice.model.OperationStatus;
import faang.school.paymentservice.model.PaymentRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class PaymentServiceValidator {
    public void validateRequestBeforeClearing(PaymentRequest paymentRequest) {
        OperationStatus paymentRequestStatus = paymentRequest.getStatus();
        if (!paymentRequestStatus.equals(OperationStatus.AUTHORIZED)) {
            log.warn("""
                    Clearing was terminated due to wrong status of request. Attempted to clear request with status {}.
                    """, paymentRequestStatus);

            throw new DataValidationException("Only pending operations (with \"AUTHORIZED\" status) can be cleared.");
        }
    }
}
