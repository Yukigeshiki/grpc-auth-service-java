package io.robothouse.grpcauth.lib.component;

import io.grpc.*;
import io.robothouse.grpcauth.lib.util.CtxConstants;
import lombok.extern.log4j.Log4j2;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Log4j2
@Component
@GlobalServerInterceptor
public class JwtAuthenticationInterceptor implements ServerInterceptor {

    private static final Metadata.Key<String> AUTHORIZATION_METADATA_KEY =
            Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);

    private final JwtDecoder jwtDecoder;

    public JwtAuthenticationInterceptor(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next
    ) {
        Context ctx;
        var authHeader = headers.get(AUTHORIZATION_METADATA_KEY);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            var token = authHeader.substring("Bearer ".length());
            try {
                var jwt = jwtDecoder.decode(token);
                log.info("JWT validation successful. Token issued by: {}", jwt.getIssuer());
                ctx = Context.current().withValue(CtxConstants.JWT_CONTEXT_KEY, Optional.of(jwt));
            } catch (JwtException e) {
                log.error("JWT validation failed: {}", e.getMessage());
                ctx = Context.current().withValue(CtxConstants.JWT_CONTEXT_KEY, Optional.empty());
            }
        } else {
            log.error("Missing or malformed Authorization header");
            ctx = Context.current().withValue(CtxConstants.JWT_CONTEXT_KEY, Optional.empty());
        }
        return Contexts.interceptCall(ctx, call, headers, next);
    }
}
