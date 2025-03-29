package io.robothouse.grpcauth.lib.util;

import io.grpc.Context;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

public class JwtConstants {
    public static final Context.Key<Optional<Jwt>> JWT_CONTEXT_KEY = Context.key("jwt");
}

