package io.robothouse.grpcauth.service;

import com.google.protobuf.Empty;
import io.grpc.Context;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.robothouse.grpcauth.lib.util.CtxConstants;
import io.robothouse.grpcauth.proto.AuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for the AuthServiceImpl gRPC service.
 */
class AuthServiceImplTest {

    private AuthServiceImpl authService;

    /** Mock observer for capturing gRPC responses. */
    private StreamObserver<AuthResponse> responseObserver;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        authService = new AuthServiceImpl();
        responseObserver = mock(StreamObserver.class);
    }

    /**
     * Tests authentication with a valid JWT token in the context.
     *
     * Given: A gRPC context containing a valid (mocked) JWT token
     * When: The authenticate method is called
     * Then: The response should indicate success with status code 0
     */
    @Test
    void authenticateWithValidJwt() {
        var jwtOptional = Optional.of(mock(Jwt.class));
        var context = Context.current()
                .withValue(CtxConstants.JWT_CONTEXT_KEY, jwtOptional)
                .withValue(CtxConstants.REQUEST_ID_CONTEXT_KEY, "test-request-id");
        var previousContext = context.attach();
        var request = Empty.getDefaultInstance();
        var responseCaptor = ArgumentCaptor.forClass(AuthResponse.class);

        authService.authenticate(request, responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        Mockito.verify(responseObserver).onCompleted();
        context.detach(previousContext);

        var response = responseCaptor.getValue();
        assertTrue(response.getPayload().getSuccess());
        assertEquals(0, response.getPayload().getStatusCode());
        assertEquals("Authentication successful.", response.getPayload().getStatusMessage());
        assertEquals("test-request-id", response.getRequestId());
    }

    /**
     * Tests authentication with an empty JWT optional in the context.
     *
     * Given: A gRPC context containing an empty Optional (no JWT)
     * When: The authenticate method is called
     * Then: An UNAUTHENTICATED status exception should be thrown
     */
    @Test
    void authenticateWithEmptyJwt() {
        var jwtOptional = Optional.<Jwt>empty();
        var context = Context.current()
                .withValue(CtxConstants.JWT_CONTEXT_KEY, jwtOptional)
                .withValue(CtxConstants.REQUEST_ID_CONTEXT_KEY, "test-request-id");
        var previousContext = context.attach();
        var request = Empty.getDefaultInstance();
        var errorCaptor = ArgumentCaptor.forClass(Throwable.class);

        authService.authenticate(request, responseObserver);

        Mockito.verify(responseObserver).onError(errorCaptor.capture());
        Mockito.verify(responseObserver, Mockito.never()).onNext(Mockito.any());
        context.detach(previousContext);

        var error = errorCaptor.getValue();
        assertInstanceOf(StatusRuntimeException.class, error);
        var statusException = (StatusRuntimeException) error;
        assertEquals(Status.UNAUTHENTICATED.getCode(), statusException.getStatus().getCode());
        assertEquals("Missing or invalid JWT token", statusException.getStatus().getDescription());
    }

    /**
     * Tests authentication when JWT context contains empty Optional.
     *
     * Given: A gRPC context with empty JWT Optional
     * When: The authenticate method is called
     * Then: An UNAUTHENTICATED status exception should be thrown
     */
    @Test
    void authenticateWithEmptyJwtOptional() {
        var context = Context.current()
                .withValue(CtxConstants.JWT_CONTEXT_KEY, Optional.<Jwt>empty())
                .withValue(CtxConstants.REQUEST_ID_CONTEXT_KEY, "test-request-id");
        var previousContext = context.attach();
        var request = Empty.getDefaultInstance();
        var errorCaptor = ArgumentCaptor.forClass(Throwable.class);

        authService.authenticate(request, responseObserver);

        Mockito.verify(responseObserver).onError(errorCaptor.capture());
        context.detach(previousContext);

        var error = errorCaptor.getValue();
        assertInstanceOf(StatusRuntimeException.class, error);
        var statusException = (StatusRuntimeException) error;
        assertEquals(Status.UNAUTHENTICATED.getCode(), statusException.getStatus().getCode());
    }

    /**
     * Tests that missing request ID defaults to "unknown".
     *
     * Given: A gRPC context with valid JWT but no request ID
     * When: The authenticate method is called
     * Then: The response should contain "unknown" as the request ID
     */
    @Test
    void authenticateWithMissingRequestId() {
        var jwtOptional = Optional.of(mock(Jwt.class));
        var context = Context.current()
                .withValue(CtxConstants.JWT_CONTEXT_KEY, jwtOptional);
        var previousContext = context.attach();
        var request = Empty.getDefaultInstance();
        var responseCaptor = ArgumentCaptor.forClass(AuthResponse.class);

        authService.authenticate(request, responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        context.detach(previousContext);

        var response = responseCaptor.getValue();
        assertEquals("unknown", response.getRequestId());
    }
}
