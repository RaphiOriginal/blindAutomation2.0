package ch.raphaelbrunner.blindautomation.rest.controller;

import ch.raphaelbrunner.blindautomation.action.Action;
import ch.raphaelbrunner.blindautomation.action.ActionType;
import ch.raphaelbrunner.blindautomation.model.Blind;
import ch.raphaelbrunner.blindautomation.model.enums.DeviceStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Arrays;
import java.util.UUID;

@RestController
public class ActionController {

    private static final Logger LOG = LoggerFactory.getLogger(ActionController.class);

    private final ObjectMapper objectMapper;

    private final KafkaTemplate<String, String> template;

    private final EntityManager entityManager;

    @Value("${app.kafka.topic}")
    private String topic;

    @Autowired
    public ActionController(final ObjectMapper objectMapper, final KafkaTemplate<String, String> template,
                            final EntityManager entityManager) {
        this.objectMapper = objectMapper;
        this.template = template;
        this.entityManager = entityManager;
    }

    @GetMapping("blindautomation/action")
    public ResponseEntity<?> executeAction(@RequestParam final UUID blindId, @RequestParam final ActionType action,
                                           @RequestParam(required = false) final Float value) {
        LOG.debug("Received action request with blindId={}, action={}, value={}", blindId, action, value);

        if (value == null && Arrays.asList(ActionType.MOVE, ActionType.TILT).contains(action)) {
            return ResponseEntity.badRequest()
                    .body(String.format("Value parameter needed for action '%s'!", action));
        }

        try {
            final Blind blind = findActiveBlind(blindId);
            final Action actionEntity = new Action();
            actionEntity.setId(UUID.randomUUID());
            actionEntity.setAction(action);
            actionEntity.setBlindId(blind.getId());
            if (value != null) {
                actionEntity.setValue(value);
            }
            final String json = objectMapper.writeValueAsString(actionEntity);
            final ProducerRecord<String, String> record = new ProducerRecord<>(
                    topic, actionEntity.getId().toString(), json
            );
            template.send(record);
            return ResponseEntity.ok(String.format(
                    "Executing %s for %s", action, blind.getName() != null ? blind.getName() : blind.getId()));
        }
        catch (final NoResultException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(String.format("No matching Blind with id=%s found.", blindId));
        } catch (JsonProcessingException e) {
            return ResponseEntity.internalServerError().body("Not able to parse Action object");
        }


    }

    private Blind findActiveBlind(final UUID blindId) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Blind> query = cb.createQuery(Blind.class);
        final Root<Blind> root = query.from(Blind.class);
        query.select(root);
        query.where(cb.and(
                cb.equal(root.get(Blind.Fields.deviceStatus), DeviceStatus.ACTIVE),
                cb.equal(root.get(Blind.Fields.id), blindId)
        ));

        return entityManager.createQuery(query).getSingleResult();
    }
}
