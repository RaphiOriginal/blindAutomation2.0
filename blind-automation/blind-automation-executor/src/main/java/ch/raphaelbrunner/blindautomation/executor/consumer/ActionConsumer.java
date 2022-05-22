package ch.raphaelbrunner.blindautomation.executor.consumer;

import ch.raphaelbrunner.blindautomation.action.Action;
import ch.raphaelbrunner.blindautomation.executor.handler.ActionExecutor;
import ch.raphaelbrunner.blindautomation.model.Blind;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@Component
@Transactional
public class ActionConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(ActionConsumer.class);

    private final EntityManager entityManager;

    private final ObjectMapper objectMapper;

    private final ActionExecutor executor;

    @Autowired
    public ActionConsumer(final EntityManager entityManager, final ObjectMapper objectMapper, final ActionExecutor executor) {
        this.entityManager = entityManager;
        this.objectMapper = objectMapper;
        this.executor = executor;
    }

    @KafkaListener(topics = { "Action" })
    public void action(@Header(name = KafkaHeaders.RECEIVED_MESSAGE_KEY) final String key,
                       @Payload final String payload) {
        LOG.debug("Received message with key={} and payload={}", key, payload);
        try {
            final Action action = objectMapper.readValue(payload, Action.class);
            final Blind blind = entityManager.find(Blind.class, action.getBlindId());
            if (blind != null) {
                executor.execute(action, blind);
            }
        } catch (JsonMappingException e) {
            LOG.error("Not able to map payload with '{}' to class {}.", payload, Action.class.getSimpleName());
        } catch (JsonProcessingException e) {
            LOG.error("Not able to deserialize payload with '{}' to class {}.", payload, Action.class.getSimpleName());
        }
    }
}
