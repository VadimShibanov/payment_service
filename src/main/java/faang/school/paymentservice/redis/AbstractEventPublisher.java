package faang.school.paymentservice.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.paymentservice.dto.PaymentRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractEventPublisher {
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic topic;


    public void publish(PaymentRequestDto event) {
        try {
            redisTemplate.convertAndSend(topic.getTopic(), objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            log.error("Serializing to json failed: {0}", e);
        }
    }
}