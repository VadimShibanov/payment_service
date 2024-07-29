package faang.school.paymentservice.controller;

import faang.school.paymentservice.config.currency.CurrencyExchangeConfig;
import faang.school.paymentservice.dto.PaymentRequestDto;
import faang.school.paymentservice.dto.PaymentResponseDto;
import faang.school.paymentservice.dto.exchange.CurrencyExchangeResponse;
import faang.school.paymentservice.exception.DataValidationException;
import faang.school.paymentservice.model.Currency;
import faang.school.paymentservice.model.OperationStatus;
import faang.school.paymentservice.service.CurrencyConverterService;
import faang.school.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentController {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");
    private static final String CONVERTING_MONEY_MESSAGE = "Dear friend! Thank you for converting money! You converted %s %s to %s %s with commission %f%%";

    private final CurrencyConverterService converterService;
    private final PaymentService paymentService;
    private final CurrencyExchangeConfig exchangeConfig;


    @PostMapping("/payment")
    public PaymentResponseDto sendPayment(@RequestBody @Valid PaymentRequestDto dto) {
        if (dto.getSenderAccountNumber().equals(dto.getReceiverAccountNumber())) {
            throw new DataValidationException("Sender and receiver cannot have the same account number.");
        }

        return paymentService.sendPayment(dto);
    }

    @PatchMapping("/payment/cancel")
    public PaymentResponseDto cancelPayment(@RequestParam @NonNull UUID paymentId) {
        return paymentService.cancelPayment(paymentId);
    }

    @PatchMapping("/payment/confirm")
    public PaymentResponseDto confirmPayment(
            @RequestParam @NonNull UUID paymentId,
            @RequestParam(required = false) BigDecimal newAmount) {
        return paymentService.confirmPayment(paymentId, newAmount);
    }

    /**
     * Получение текущего соотношения валют к доллару из внешнего источника
     */
    @GetMapping("currency")
    public CurrencyExchangeResponse getCurrentCurrencyExchangeRate() {
        return converterService.getCurrentCurrencyExchangeRate();
    }

    /**
     * Конвертация одной валюты в другую
     *
     * @param dto            Объект для конвертации
     * @param targetCurrency целевая валюта
     * @return Объект результата конвертации
     */
    @PostMapping("exchange")
    public ResponseEntity<PaymentResponseDto> exchangeCurrency(@RequestBody @Validated PaymentRequestDto dto, @RequestParam Currency targetCurrency) {
        BigDecimal newAmount = converterService.convertWithCommission(dto, targetCurrency);

        String message = String.format(
                CONVERTING_MONEY_MESSAGE,
                DECIMAL_FORMAT.format(dto.getAmount()),
                dto.getCurrency(),
                DECIMAL_FORMAT.format(newAmount),
                targetCurrency,
                exchangeConfig.getCommission()
        );

        return ResponseEntity.ok(PaymentResponseDto.builder()
                .status(OperationStatus.CONFIRMED)
                .senderAccountNumber(dto.getSenderAccountNumber())
                .amount(newAmount)
                .currency(targetCurrency)
                .message(message)
                .build()
        );
    }
}
