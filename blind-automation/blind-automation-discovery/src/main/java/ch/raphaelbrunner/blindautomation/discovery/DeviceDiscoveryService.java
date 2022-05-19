package ch.raphaelbrunner.blindautomation.discovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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

    @Autowired
    public DeviceDiscoveryService(final DeviceListener deviceListener) {
        this.deviceListener = deviceListener;
    }

    @Scheduled(fixedRate=30000)
    public void findDevices() {
        try (final JmDNS jmdns = JmDNS.create(InetAddress.getLocalHost(), "Device Discovery Service")) {
            // Add a service listener
            jmdns.addServiceListener(TYPE, deviceListener);
            final ServiceInfo[] list = jmdns.list(TYPE);
            Arrays.stream(list).filter(i -> LOG.isDebugEnabled())
                    .forEach(i -> LOG.trace("Device '{}' found.", i.getName()));
        } catch (final IOException e) {
            LOG.warn(e.getMessage());
        }
    }
}
