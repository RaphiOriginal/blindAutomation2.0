package ch.raphaelbrunner.blindautomation.db.consumer;

import ch.raphaelbrunner.blindautomation.discovery.entity.Device;
import ch.raphaelbrunner.blindautomation.discovery.enumeration.Action;
import ch.raphaelbrunner.blindautomation.model.Blind;
import ch.raphaelbrunner.blindautomation.model.enums.DeviceStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.UUID;

@Component
@Transactional
public class DeviceConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceConsumer.class);

    private final EntityManager entityManager;

    private final ObjectMapper objectMapper;

    @Autowired
    public DeviceConsumer(final EntityManager entityManager, final ObjectMapper objectMapper) {
        this.entityManager = entityManager;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = { "Device" })
    public void device(@Header(name = KafkaHeaders.RECEIVED_MESSAGE_KEY) final String key, @Payload final String payload) {
        LOG.debug("Received message with key={} and payload={}", key, payload);
        try {
            final Device device = objectMapper.readValue(payload, Device.class);
            final List<Blind> existingBlinds = fetchBlindByDeviceId(device.getId());
            if (existingBlinds.isEmpty() && Action.ADD.equals(device.getAction())) {
                // create new Blind
                final Blind blind = new Blind();
                blind.setId(UUID.randomUUID());
                blind.setDeviceId(device.getId());
                blind.setUrl(device.getUrl());
                blind.setDeviceStatus(DeviceStatus.ACTIVE);
                entityManager.persist(blind);
                LOG.info("Created and activated Blind with deviceId={}", blind.getDeviceId());
            }
            else if (!existingBlinds.isEmpty() && Action.REMOVE.equals(device.getAction())) {
                // deactivate Blind
                existingBlinds.forEach(blind -> {
                    if (DeviceStatus.ACTIVE.equals(blind.getDeviceStatus())) {
                        blind.setDeviceStatus(DeviceStatus.INACTIVE);
                        entityManager.persist(blind);
                        LOG.info("Deactivated Blind with deviceId={} and name={}", blind.getDeviceId(), blind.getName());
                    }
                });
            }
            else if (!existingBlinds.isEmpty() && Action.ADD.equals(device.getAction())) {
                // reactivate Blind
                existingBlinds.forEach(blind -> {
                    if (DeviceStatus.INACTIVE.equals(blind.getDeviceStatus())) {
                        blind.setDeviceStatus(DeviceStatus.ACTIVE);
                        entityManager.persist(blind);
                        LOG.info("Activated Blind with deviceId={} and name={}", blind.getDeviceId(), blind.getName());
                    }
                });
            }
            else {
                LOG.trace("Nothing to do for Device {}", device);
            }
        } catch (JsonProcessingException e) {
            LOG.error("Not able to deserialize Device string '{}'", payload, e);
        }
    }

    private List<Blind> fetchBlindByDeviceId(final String id) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Blind> query = cb.createQuery(Blind.class);
        final Root<Blind> root = query.from(Blind.class);
        query.select(root);
        query.where(cb.equal(root.get(Blind.Fields.deviceId), id));

        final TypedQuery<Blind> q = entityManager.createQuery(query);
        return q.getResultList();
    }
}
