package io.robothouse.grpcauth.lib.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;

@Configuration
public class JwtConfig {

    private final String projectId;

    public JwtConfig(@Value("${spring.authentication.project_id}") String projectId) {
        this.projectId = projectId;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        var issuerUri = "https://securetoken.google.com/" + projectId;
        return JwtDecoders.fromIssuerLocation(issuerUri);
    }
}
