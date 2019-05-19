package com.upgrad.quora.api;

import com.upgrad.quora.service.ServiceConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * A Configuration class that can declare one or more @Bean methods and trigger auto-configuration and component scanning.
 * This class launches a Spring Application from Java main method.
 */
@SpringBootApplication
@Import(ServiceConfiguration.class)
public class QuoraApiApplication {
    public static void main(String[] args) {
        SpringApplication mainApplication = new SpringApplication(QuoraApiApplication.class);
        mainApplication.setDefaultProperties(Collections.singletonMap("server.port","8080"));
        mainApplication.run(args);
    }
}
//run on custom port
@Component
class CustomPort implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {

    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        factory.setPort(8080);
    }
}
