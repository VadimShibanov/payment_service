package faang.school.paymentservice.repository;

import faang.school.paymentservice.model.OperationStatus;
import faang.school.paymentservice.model.PaymentRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRequestJpaRepository extends JpaRepository<PaymentRequest, UUID> {
    @Query("""
            SELECT p FROM PaymentRequest p
            WHERE p.status = :status AND p.clearScheduledAt < CURRENT_TIMESTAMP
            ORDER BY p.updatedAt
            DESC
            """)
    List<PaymentRequest> findReadyToClearRequests(OperationStatus status, Pageable pageable);

    @Query(nativeQuery = true, value = """
            SELECT EXISTS(
                SELECT * FROM payment_request pr
                WHERE pr.id = :id AND pr.operation_status NOT IN ('FAILED', 'REFUSED'))
            """)
    boolean existsByIdAndIsNotFailed(UUID id);
}
