package com.github.crimscon.autoconfigure.springdoc.config;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.TestConfiguration;

@SpringBootApplication(scanBasePackages = "com.github.crimscon.autoconfigure.springdoc")
public class SpringDocJaxRsConfig {

    @TestConfiguration
    public static class JaxRsConfig extends ResourceConfig {
        public JaxRsConfig() {
            packages("com.github.crimscon.autoconfigure.springdoc.resource");
        }
    }

}
