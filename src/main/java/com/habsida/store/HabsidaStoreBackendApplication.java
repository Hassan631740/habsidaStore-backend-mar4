package com.habsida.store;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("com.habsida.store.config")
public class HabsidaStoreBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(HabsidaStoreBackendApplication.class, args);
    }
}
