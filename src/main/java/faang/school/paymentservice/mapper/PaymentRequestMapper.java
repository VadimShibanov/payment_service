package faang.school.paymentservice.mapper;

import faang.school.paymentservice.dto.PaymentRequestDto;
import faang.school.paymentservice.dto.PaymentResponseDto;
import faang.school.paymentservice.model.PaymentRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentRequestMapper {
    @Mapping(target = "id", source = "paymentId")
    PaymentRequest toModel(PaymentRequestDto dto);

    @Mapping(target = "paymentId", source = "id")
    PaymentResponseDto toPaymentResponse(PaymentRequest model);

    PaymentResponseDto toPaymentResponse(PaymentRequestDto dto);

    @Mapping(target = "paymentId", source = "id")
    PaymentRequestDto toDto(PaymentRequest pendingRequest);
}
