package ch.raphaelbrunner.blindautomation.rest;

import ch.raphaelbrunner.blindautomation.common.configuration.JpaConfiguration;
import ch.raphaelbrunner.blindautomation.common.configuration.KafkaConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.EnableKafka;

import javax.annotation.PreDestroy;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
@EnableKafka
@Import(value = {
        JpaConfiguration.class,
        KafkaConfig.class
})
public class Application {
    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    private JmDNS jmDNS;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @EventListener
    public void onStartup(final ApplicationStartedEvent ignoredEvent) {
        try {
            jmDNS = JmDNS.create(InetAddress.getLocalHost());

            // Register a service
            final ServiceInfo serviceInfo = ServiceInfo.create(
                    "_http._tcp.local.",
                    "bindautomation",
                    8080,
                    "Application to setup and control the blind automation");
            jmDNS.registerService(serviceInfo);
            LOG.info("Successfully registered application on mDNS.");
        } catch (UnknownHostException e) {
            LOG.error("Not able to setup mDNS as host is unknown.", e);
        } catch (IOException e) {
            LOG.error("Not able to setup mDNS as there is some IO trouble.", e);
        }
    }

    @PreDestroy
    public void onShutdown() throws IOException {
        if (jmDNS != null) {
            jmDNS.unregisterAllServices();
            jmDNS.close();
        }
    }
}
