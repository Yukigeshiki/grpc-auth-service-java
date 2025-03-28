package io.robothouse.grpcauth.lib.component;

import io.grpc.*;
import io.robothouse.grpcauth.lib.util.JwtConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

@Component
@GlobalServerInterceptor
public class JwtAuthenticationInterceptor implements ServerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationInterceptor.class);
    private static final Metadata.Key<String> AUTHORIZATION_METADATA_KEY =
            Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);

    private final JwtDecoder jwtDecoder;

    public JwtAuthenticationInterceptor(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public <Req, Res> ServerCall.Listener<Req> interceptCall(
            ServerCall<Req, Res> call,
            Metadata headers,
            ServerCallHandler<Req, Res> next
    ) {

        log.info("I ran!!!!");

        String authHeader = headers.get(AUTHORIZATION_METADATA_KEY);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring("Bearer ".length());
            try {
                Jwt jwt = jwtDecoder.decode(token);
                Context ctx = Context.current().withValue(JwtConstants.JWT_CONTEXT_KEY, jwt);
                return Contexts.interceptCall(ctx, call, headers, next);
            } catch (JwtException e) {
                log.error("JWT validation failed", e);
                call.close(Status.UNAUTHENTICATED.withDescription("Invalid JWT Token: " + e.getMessage()), headers);
                return new ServerCall.Listener<>() {
                };
            }
        } else {
            log.warn("Missing or malformed Authorization header");
            call.close(Status.UNAUTHENTICATED.withDescription("Missing or malformed Authorization header"), headers);
            return new ServerCall.Listener<>() {
            };
        }
    }
}