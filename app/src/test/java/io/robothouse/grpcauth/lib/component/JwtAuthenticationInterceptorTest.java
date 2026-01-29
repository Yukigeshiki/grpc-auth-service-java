package io.robothouse.grpcauth.lib.component;

import io.grpc.*;
import io.robothouse.grpcauth.lib.util.CtxConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.core.OAuth2Error;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JwtAuthenticationInterceptor.
 */
class JwtAuthenticationInterceptorTest {

    private static final Metadata.Key<String> AUTHORIZATION_KEY =
            Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);

    private JwtDecoder jwtDecoder;
    private JwtAuthenticationInterceptor interceptor;
    private ServerCall<Object, Object> serverCall;
    private ServerCallHandler<Object, Object> nextHandler;
    private Metadata headers;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        jwtDecoder = mock(JwtDecoder.class);
        interceptor = new JwtAuthenticationInterceptor(jwtDecoder);
        serverCall = mock(ServerCall.class);
        nextHandler = mock(ServerCallHandler.class);
        headers = new Metadata();

        when(nextHandler.startCall(any(), any())).thenReturn(new ServerCall.Listener<>() {});
    }

    @Test
    void interceptCallWithValidToken() throws Exception {
        var token = "valid.jwt.token";
        headers.put(AUTHORIZATION_KEY, "Bearer " + token);
        var jwt = mock(Jwt.class);
        when(jwt.getIssuer()).thenReturn(java.net.URI.create("https://issuer.example.com").toURL());
        when(jwtDecoder.decode(token)).thenReturn(jwt);

        interceptor.interceptCall(serverCall, headers, nextHandler);

        verify(jwtDecoder).decode(token);
        verify(nextHandler).startCall(any(), eq(headers));
    }

    @Test
    void interceptCallWithInvalidToken() {
        var token = "invalid.jwt.token";
        headers.put(AUTHORIZATION_KEY, "Bearer " + token);
        when(jwtDecoder.decode(token)).thenThrow(new JwtException("Invalid token"));

        interceptor.interceptCall(serverCall, headers, nextHandler);

        verify(jwtDecoder).decode(token);
        verify(nextHandler).startCall(any(), eq(headers));
    }

    @Test
    void interceptCallWithExpiredToken() {
        var token = "expired.jwt.token";
        headers.put(AUTHORIZATION_KEY, "Bearer " + token);
        var expiredError = new OAuth2Error("exp", "Token expired", null);
        when(jwtDecoder.decode(token)).thenThrow(
                new JwtValidationException("Token expired", Collections.singleton(expiredError)));

        interceptor.interceptCall(serverCall, headers, nextHandler);

        verify(jwtDecoder).decode(token);
        verify(nextHandler).startCall(any(), eq(headers));
    }

    @Test
    void interceptCallWithMissingAuthHeader() {
        interceptor.interceptCall(serverCall, headers, nextHandler);

        verify(jwtDecoder, never()).decode(any());
        verify(nextHandler).startCall(any(), eq(headers));
    }

    @Test
    void interceptCallWithMalformedAuthHeader() {
        headers.put(AUTHORIZATION_KEY, "Basic dXNlcjpwYXNz");

        interceptor.interceptCall(serverCall, headers, nextHandler);

        verify(jwtDecoder, never()).decode(any());
        verify(nextHandler).startCall(any(), eq(headers));
    }

    @Test
    void interceptCallWithEmptyBearerToken() {
        headers.put(AUTHORIZATION_KEY, "Bearer ");
        when(jwtDecoder.decode("")).thenThrow(new JwtException("Empty token"));

        interceptor.interceptCall(serverCall, headers, nextHandler);

        verify(jwtDecoder).decode("");
        verify(nextHandler).startCall(any(), eq(headers));
    }
}
