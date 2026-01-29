package io.robothouse.grpcauth.lib.util;

import io.grpc.Context;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

/**
 * Utility class containing gRPC context key constants for cross-cutting concerns.
 */
public final class CtxConstants {

    private CtxConstants() {}

    /**
     * Context key for storing the authenticated JWT token.
     *
     * <p>This key holds an Optional containing the decoded JWT if authentication
     * was successful, or an empty Optional if the token was missing, malformed, or invalid.</p>
     */
    public static final Context.Key<Optional<Jwt>> JWT_CONTEXT_KEY = Context.key("jwt");

    /**
     * Context key for storing the unique request identifier.
     */
    public static final Context.Key<String> REQUEST_ID_CONTEXT_KEY = Context.key("requestId");
}
