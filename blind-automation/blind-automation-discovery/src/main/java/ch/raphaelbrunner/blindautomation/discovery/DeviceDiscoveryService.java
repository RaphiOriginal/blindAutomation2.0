package ch.raphaelbrunner.blindautomation.discovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;

@Service
public class DeviceDiscoveryService {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceDiscoveryService.class);

    private static final String TYPE = "_http._tcp.local.";

    private final DeviceListener deviceListener;

    private JmDNS jmdns;

    @Autowired
    public DeviceDiscoveryService(final DeviceListener deviceListener) {
        this.deviceListener = deviceListener;
    }

    @EventListener
    public void findDevices(final ApplicationStartedEvent ignoredEvent) {
        try {
            jmdns = JmDNS.create(InetAddress.getLocalHost(), "Device Discovery Service");
            // Add a service listener
            jmdns.addServiceListener(TYPE, deviceListener);
            final ServiceInfo[] list = jmdns.list(TYPE);
            Arrays.stream(list).filter(i -> LOG.isDebugEnabled())
                    .forEach(i -> LOG.debug("Device '{}' found.", i.getName()));
        } catch (final IOException e) {
            LOG.warn(e.getMessage());
        }
    }

    @PreDestroy
    public void shutdown() throws IOException {
        if (jmdns != null) {
            jmdns.close();
        }
    }
}
