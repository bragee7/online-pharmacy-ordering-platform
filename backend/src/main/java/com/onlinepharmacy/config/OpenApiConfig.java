package com.onlinepharmacy.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI pharmacyOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Online Pharmacy Ordering Platform API")
                        .description("""
                                REST API for the Online Pharmacy Ordering Platform capstone project.

                                Roles: CUSTOMER, PHARMACIST, ADMIN. Protected endpoints require a
                                Bearer token obtained from /api/auth/login.
                                """)
                        .version("1.0.0")
                        .contact(new Contact().name("Capstone Team")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}