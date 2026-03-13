package com.habsida.store.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Strips MySQL-style query params from the datasource URL so PostgreSQL driver accepts it
 * (e.g. if .env has useUnicode=true&characterEncoding=utf8 from an old MySQL URL).
 * Runs after other processors (e.g. dotenv) so DATABASE_URL is available; overrides it
 * so ${DATABASE_URL} in application.properties resolves to the clean URL.
 */
public class DatasourceUrlSanitizer implements EnvironmentPostProcessor, Ordered {

    private static final String DATABASE_URL_KEY = "DATABASE_URL";
    private static final String DATASOURCE_URL_KEY = "spring.datasource.url";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String url = environment.getProperty(DATABASE_URL_KEY);
        if (url == null) {
            url = environment.getProperty(DATASOURCE_URL_KEY);
        }
        if (url == null || (!url.contains("useUnicode") && !url.contains("characterEncoding") && url.startsWith("jdbc:"))) {
            return;
        }
        // Fix missing "j" (jdbc -> dbc) and strip MySQL query params
        if (!url.startsWith("jdbc:")) {
            Logger.getLogger(DatasourceUrlSanitizer.class.getName())
                    .warning("DATABASE_URL did not start with 'jdbc:'; prepended 'j'. Result: j" + url + " — please fix the URL in .env");
        }
        String base = url.startsWith("jdbc:") ? url : "j" + url;
        String clean = base.contains("?") ? base.substring(0, base.indexOf('?')) : base;
        Map<String, Object> overrides = new java.util.LinkedHashMap<>();
        overrides.put(DATABASE_URL_KEY, clean);
        overrides.put(DATASOURCE_URL_KEY, clean);
        environment.getPropertySources().addFirst(
                new MapPropertySource("datasourceUrlSanitizer", overrides));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
