package faang.school.paymentservice.service;

import faang.school.paymentservice.client.CurrencyConverterClient;
import faang.school.paymentservice.config.currency.CurrencyExchangeConfig;
import faang.school.paymentservice.dto.PaymentRequestDto;
import faang.school.paymentservice.dto.exchange.CurrencyExchangeResponse;
import faang.school.paymentservice.model.Currency;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyConverterService {
    private final CurrencyConverterClient currencyConverterClient;
    private final CurrencyExchangeConfig exchangeConfig;

    public CurrencyExchangeResponse getCurrentCurrencyExchangeRate() {
        return currencyConverterClient.getCurrentCurrencyExchangeRate(exchangeConfig.getAppId());
    }

    public BigDecimal convertWithCommission(PaymentRequestDto dto, Currency targetCurrency) {
        BigDecimal newAmount = getAmountInNewCurrency(dto, targetCurrency, getCurrentCurrencyExchangeRate());
        return addCommision(newAmount);
    }

    private BigDecimal addCommision(BigDecimal amount) {
        BigDecimal commission = BigDecimal.valueOf(1).add(BigDecimal.valueOf(exchangeConfig.getCommission() / 100.0));
        return amount.multiply(commission);
    }

    private BigDecimal getAmountInNewCurrency(
            PaymentRequestDto dto,
            Currency targetCurrency,
            CurrencyExchangeResponse currentCurrencyExchange
    ) {
        BigDecimal amount = dto.getAmount();
        BigDecimal targetRate = currentCurrencyExchange.getRate(targetCurrency);
        BigDecimal baseRate = currentCurrencyExchange.getRate(dto.getCurrency());
        return (amount.multiply(targetRate)).divide(baseRate, 2, RoundingMode.HALF_UP);
    }
}
