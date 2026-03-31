package com.habsida.store.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Habsida Store API")
                        .description("REST API for Habsida Store. **Auth** (login/register) then use **Authorize** with the returned JWT. "
                                + "**Core** = catalog: stores, categories, products, modifiers. "
                                + "Validation errors return 400 with a `details` array of `{ field, message }`.")
                        .version("1.0")
                        .contact(new Contact().name("Habsida Store")))
                .tags(List.of(
                        new Tag().name("Auth").description("Login, register, refresh token"),
                        new Tag().name("Stores").description("Store management (admin: any store; merchant: assigned stores)"),
                        new Tag().name("Categories").description("Category CRUD per store"),
                        new Tag().name("Products").description("Product CRUD, filters, pause ordering"),
                        new Tag().name("Modifiers").description("Modifier groups, options, assign groups to products")
                ))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT from POST /api/auth/login or /api/auth/register")));
    }
}
