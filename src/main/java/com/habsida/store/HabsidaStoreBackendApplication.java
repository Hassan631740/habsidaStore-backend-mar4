package com.habsida.store;

import com.habsida.store.config.CorsProperties;
import com.habsida.store.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, CorsProperties.class})
public class HabsidaStoreBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(HabsidaStoreBackendApplication.class, args);
    }
}
