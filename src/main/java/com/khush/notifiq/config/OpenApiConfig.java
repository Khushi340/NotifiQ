package com.khush.notifiq.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI notifiqOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NotifiQ API")
                        .version("1.0")
                        .description("Reliable multi-channel notification delivery platform with async delivery, retries, dead-letter handling, replay, preferences, and stats."));
    }
}