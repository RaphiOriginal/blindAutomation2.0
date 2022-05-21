package ch.raphaelbrunner.blindautomation.rest;

import ch.raphaelbrunner.blindautomation.common.configuration.JpaConfiguration;
import ch.raphaelbrunner.blindautomation.common.configuration.KafkaConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
@Import(value = {
        JpaConfiguration.class,
        KafkaConfig.class
})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
