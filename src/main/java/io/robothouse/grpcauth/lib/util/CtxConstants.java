package io.robothouse.grpcauth.lib.util;

import io.grpc.Context;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

public class CtxConstants {
    public static final Context.Key<Optional<Jwt>> JWT_CONTEXT_KEY = Context.key("jwt");
    public static final Context.Key<String> REQUEST_ID = Context.key("requestId");
}
