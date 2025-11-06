package com.example.springssodemo.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3 (Swagger) documentation.
 * Accessible at: http://localhost:8080/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI apiDocs() {
        return new OpenAPI()
                .info(new Info()
                        .title("Admin Dashboard API")
                        .description("REST API for User Management and SSO Configurations")
                        .version("3.0.0")
                        .contact(new Contact().name("MHTechin Team").email("support@mhtechin.com")));
    }
}
