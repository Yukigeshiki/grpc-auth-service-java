package io.robothouse.grpcauth.lib.component;

import io.grpc.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.core.OAuth2Error;

import java.util.Collections;

import static org.mockito.Mockito.*;

/**
 * Unit tests for JwtAuthenticationInterceptor.
 */
class JwtAuthenticationInterceptorTest {

    /**
     * Metadata key for the Authorization header used in test requests.
     */
    private static final Metadata.Key<String> AUTHORIZATION_KEY =
            Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);

    /**
     * Mock JWT decoder for token validation.
     */
    private JwtDecoder jwtDecoder;

    private JwtAuthenticationInterceptor interceptor;

    /**
     * Mock gRPC server call for interceptor testing.
     */
    private ServerCall<Object, Object> serverCall;

    /**
     * Mock handler for the next interceptor in the chain.
     */
    private ServerCallHandler<Object, Object> nextHandler;

    /**
     * gRPC metadata headers passed to the interceptor.
     */
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

    /**
     * Tests authentication with a valid Bearer token.
     *
     * <p>Given: A request with a valid Bearer token in the Authorization header.</p>
     * <p>When: The interceptor processes the call.</p>
     * <p>Then: The token should be decoded and the call should proceed to the next handler.</p>
     */
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

    /**
     * Tests authentication with an invalid JWT token.
     *
     * <p>Given: A request with an invalid Bearer token that fails decoding.</p>
     * <p>When: The interceptor processes the call.</p>
     * <p>Then: The token should be decoded (and fail) and the call should still proceed.</p>
     */
    @Test
    void interceptCallWithInvalidToken() {
        var token = "invalid.jwt.token";
        headers.put(AUTHORIZATION_KEY, "Bearer " + token);
        when(jwtDecoder.decode(token)).thenThrow(new JwtException("Invalid token"));

        interceptor.interceptCall(serverCall, headers, nextHandler);

        verify(jwtDecoder).decode(token);
        verify(nextHandler).startCall(any(), eq(headers));
    }

    /**
     * Tests authentication with an expired JWT token.
     *
     * <p>Given: A request with an expired Bearer token that triggers a JwtValidationException.</p>
     * <p>When: The interceptor processes the call.</p>
     * <p>Then: The token should be decoded (and fail) and the call should still proceed.</p>
     */
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

    /**
     * Tests authentication when the Authorization header is missing.
     *
     * <p>Given: A request with no Authorization header.</p>
     * <p>When: The interceptor processes the call.</p>
     * <p>Then: The decoder should not be called and the call should proceed.</p>
     */
    @Test
    void interceptCallWithMissingAuthHeader() {
        interceptor.interceptCall(serverCall, headers, nextHandler);

        verify(jwtDecoder, never()).decode(any());
        verify(nextHandler).startCall(any(), eq(headers));
    }

    /**
     * Tests authentication with a malformed Authorization header (non-Bearer scheme).
     *
     * <p>Given: A request with a Basic auth header instead of Bearer.</p>
     * <p>When: The interceptor processes the call.</p>
     * <p>Then: The decoder should not be called and the call should proceed.</p>
     */
    @Test
    void interceptCallWithMalformedAuthHeader() {
        headers.put(AUTHORIZATION_KEY, "Basic dXNlcjpwYXNz");

        interceptor.interceptCall(serverCall, headers, nextHandler);

        verify(jwtDecoder, never()).decode(any());
        verify(nextHandler).startCall(any(), eq(headers));
    }

    /**
     * Tests authentication with an empty Bearer token value.
     *
     * <p>Given: A request with "Bearer " but no actual token value.</p>
     * <p>When: The interceptor processes the call.</p>
     * <p>Then: The decoder should attempt to decode the empty string and the call should proceed.</p>
     */
    @Test
    void interceptCallWithEmptyBearerToken() {
        headers.put(AUTHORIZATION_KEY, "Bearer ");
        when(jwtDecoder.decode("")).thenThrow(new JwtException("Empty token"));

        interceptor.interceptCall(serverCall, headers, nextHandler);

        verify(jwtDecoder).decode("");
        verify(nextHandler).startCall(any(), eq(headers));
    }
}
