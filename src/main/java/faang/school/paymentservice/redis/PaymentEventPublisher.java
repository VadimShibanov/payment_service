package faang.school.paymentservice.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;


@Component
public class PaymentEventPublisher extends AbstractEventPublisher {
    public PaymentEventPublisher(
            ObjectMapper objectMapper,
            RedisTemplate<String, Object> redisTemplate,
            @Qualifier("paymentTopic") ChannelTopic topic) {
        super(objectMapper, redisTemplate, topic);
    }
}
