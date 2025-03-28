package io.robothouse.grpcauth.lib.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;

@Configuration
public class JwtConfig {

    @Value("${spring.authentication.project_id}")
    private String PROJECT_ID;

    @Bean
    public JwtDecoder jwtDecoder() {
        String issuerUri = "https://securetoken.google.com/" + PROJECT_ID;
        return JwtDecoders.fromIssuerLocation(issuerUri);
    }
}
