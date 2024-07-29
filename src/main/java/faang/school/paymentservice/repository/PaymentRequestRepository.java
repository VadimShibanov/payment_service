package faang.school.paymentservice.repository;

import faang.school.paymentservice.model.OperationStatus;
import faang.school.paymentservice.model.PaymentRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PaymentRequestRepository {
    private final PaymentRequestJpaRepository repository;


    public boolean existsByIdAndStatusIsNotFailed(UUID id) {
        return repository.existsByIdAndIsNotFailed(id);
    }

    public List<PaymentRequest> findReadyToClearRequests() {
        return repository.findReadyToClearRequests(OperationStatus.AUTHORIZED, Pageable.ofSize(5));
    }

    public PaymentRequest save(PaymentRequest paymentRequest) {
        return repository.save(paymentRequest);
    }

    public PaymentRequest findById(UUID id) {
        return repository
                .findById(id)
                .orElseThrow(() -> {
                    String message = String.format("There is no any payment requests with number %s.", id);
                    throw new EntityNotFoundException(message);
                });
    }
}
