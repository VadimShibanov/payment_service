package faang.school.paymentservice.config.currency;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "currency.exchange")
@Data
public class CurrencyExchangeConfig {
    private String url;
    private String appId;
    private Double commission;
}
