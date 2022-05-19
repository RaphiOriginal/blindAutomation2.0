package ch.raphaelbrunner.blindautomation.discovery;

import ch.raphaelbrunner.blindautomation.discovery.entity.Device;
import ch.raphaelbrunner.blindautomation.discovery.enumeration.Action;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import java.util.UUID;

@Component
public class DeviceListener implements ServiceListener {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceListener.class);

    private static final String SHELLY_PREFIX = "shelly";

    private final KafkaTemplate<String, String> template;

    private final ObjectMapper mapper;

    @Value("${app.kafka.topic}")
    private String topic;

    @Autowired
    public DeviceListener(final KafkaTemplate<String, String> template, final ObjectMapper mapper) {
        this.template = template;
        this.mapper = mapper;
    }

    @Override
    public void serviceAdded(final ServiceEvent event) {
        if (event.getName().startsWith(SHELLY_PREFIX)) {
            LOG.debug("Added event: {}", event.getInfo());
        }
    }

    @Override
    public void serviceRemoved(final ServiceEvent event) {
        if (event.getName().startsWith(SHELLY_PREFIX)) {
            try {
                final ServiceInfo info = event.getInfo();
                LOG.debug("Removed event: {}", info);
                final Device device = buildDevice(info, Action.REMOVE);
                final ProducerRecord<String, String> record = new ProducerRecord<>(
                        topic,
                        UUID.randomUUID().toString(),
                        mapper.writeValueAsString(device)
                );
                template.send(record);
            }
            catch (final JsonProcessingException e) {
                LOG.error("Not able to map Device '{}'.", event.getInfo().getName(), e);
            }
        }
    }

    @Override
    public void serviceResolved(final ServiceEvent event) {
            if (event.getName().startsWith(SHELLY_PREFIX)) {
                try {
                    final ServiceInfo info = event.getInfo();
                    LOG.debug("Resolved event: {}", info);
                    final Device device = buildDevice(info, Action.ADD);
                    final ProducerRecord<String, String> record = new ProducerRecord<>(
                            topic,
                            UUID.randomUUID().toString(),
                            mapper.writeValueAsString(device)
                    );
                    template.send(record);
                }
                catch (final JsonProcessingException e) {
                    LOG.error("Not able to map device '{}'.", event.getInfo().getName(), e);
                }
            }
    }

    private Device buildDevice(final ServiceInfo info, final Action action) {
        final String id = extractId(info.getName());
        return buildDevice(id, info.getHostAddresses()[0], action);
    }
    private Device buildDevice(final String id, final String host, final Action action) {
        final Device device = new Device();
        device.setId(id);
        device.setUrl(host);
        device.setAction(action);
        return device;
    }

    private String extractId(final String name) {
        return name.substring(name.lastIndexOf("-") + 1);
    }
}
