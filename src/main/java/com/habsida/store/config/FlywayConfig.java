package com.habsida.store.config;

import com.habsida.store.flyway.V3__AlterIdsToBigint;
import org.springframework.boot.flyway.autoconfigure.FlywayConfigurationCustomizer;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class FlywayConfig {

    @Bean
    public FlywayConfigurationCustomizer flywayConfigurationCustomizer() {
        return configuration -> configuration.javaMigrations(new V3__AlterIdsToBigint());
    }

    /**
     * Run repair before migrate so failed migrations (e.g. version 3) are cleared
     * and can be re-run. Only active in dev; in production run repair manually if needed.
     */
    @Bean
    @Profile("dev")
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            flyway.repair();
            flyway.migrate();
        };
    }
}
