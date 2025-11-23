package com.techlab.picadito.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Picadito API")
                        .version("1.0.0")
                        .description("API REST para sistema de gestión y reserva de partidos de fútbol")
                        .contact(new Contact()
                                .name("TechLab")
                                .email("info@techlab.com")));
    }
}

