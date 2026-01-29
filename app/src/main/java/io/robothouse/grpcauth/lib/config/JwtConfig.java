package io.robothouse.grpcauth.lib.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;

/**
 * Spring configuration class for JWT authentication setup.
 */
@Configuration
public class JwtConfig {

    /**
     * The Firebase project ID used to construct the JWT issuer URI.
     *
     * <p>Injected from the application configuration property 'spring.authentication.project_id'.</p>
     */
    private final String projectId;

    /**
     * The base issuer URI for JWT validation.
     *
     * <p>Injected from the application configuration property 'spring.authentication.issuer_uri'.</p>
     */
    private final String issuerUri;

    public JwtConfig(
            @Value("${spring.authentication.project_id}") String projectId,
            @Value("${spring.authentication.issuer_uri}") String issuerUri) {
        this.projectId = projectId;
        this.issuerUri = issuerUri;
    }

    /**
     * Creates and configures a JwtDecoder bean for Firebase JWT validation.
     *
     * @return a configured JwtDecoder instance for validating Firebase-issued tokens
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        var fullIssuerUri = issuerUri + projectId;
        return JwtDecoders.fromIssuerLocation(fullIssuerUri);
    }
}
