package io.robothouse.service;

import com.google.protobuf.Empty;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;
import io.robothouse.grpcauth.lib.util.CtxConstants;
import io.robothouse.grpcauth.proto.ValidateTokenResponse;
import io.robothouse.grpcauth.service.TokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class TokenServiceImplTest {

    private TokenServiceImpl tokenService;
    private StreamObserver<ValidateTokenResponse> responseObserver;

    @BeforeEach
    void setUp() {
        tokenService = new TokenServiceImpl();
        responseObserver = mock(StreamObserver.class);
    }

    @Test
    void validateTokenWithValidJwt() {
        Optional<Jwt> jwtOptional = Optional.of(mock(Jwt.class));
        Context context = Context.current()
                .withValue(CtxConstants.JWT_CONTEXT_KEY, jwtOptional)
                .withValue(CtxConstants.REQUEST_ID, "");
        Context previousContext = context.attach();
        Empty request = Empty.getDefaultInstance();
        ArgumentCaptor<ValidateTokenResponse> responseCaptor = ArgumentCaptor.forClass(ValidateTokenResponse.class);

        tokenService.validateToken(request, responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        Mockito.verify(responseObserver).onCompleted();
        context.detach(previousContext);

        ValidateTokenResponse response = responseCaptor.getValue();
        assertTrue(response.getPayload().getSuccess());
        assertEquals(0, response.getPayload().getStatusCode());
        assertEquals("Authentication successful.", response.getPayload().getStatusMessage());
    }

    @Test
    void validateTokenWithInvalidJwt() {
        Optional<Jwt> jwtOptional = Optional.empty();
        Context context = Context.current()
                .withValue(CtxConstants.JWT_CONTEXT_KEY, jwtOptional)
                .withValue(CtxConstants.REQUEST_ID, "");
        Context previousContext = context.attach();
        Empty request = Empty.getDefaultInstance();
        ArgumentCaptor<ValidateTokenResponse> responseCaptor = ArgumentCaptor.forClass(ValidateTokenResponse.class);

        tokenService.validateToken(request, responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        Mockito.verify(responseObserver).onCompleted();
        context.detach(previousContext);

        ValidateTokenResponse response = responseCaptor.getValue();
        assertFalse(response.getPayload().getSuccess());
        assertEquals(16, response.getPayload().getStatusCode());
        assertEquals("Authentication failed: Missing or invalid JWT token.", response.getPayload().getStatusMessage());
    }

    @Test
    void validateTokenWithNullJwt() {
        Context context = Context.current()
                .withValue(CtxConstants.JWT_CONTEXT_KEY, Optional.empty())
                .withValue(CtxConstants.REQUEST_ID, "");
        ;
        Context previousContext = context.attach();
        Empty request = Empty.getDefaultInstance();
        ArgumentCaptor<ValidateTokenResponse> responseCaptor = ArgumentCaptor.forClass(ValidateTokenResponse.class);

        tokenService.validateToken(request, responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        Mockito.verify(responseObserver).onCompleted();
        context.detach(previousContext);

        ValidateTokenResponse response = responseCaptor.getValue();
        assertFalse(response.getPayload().getSuccess());
        assertEquals(16, response.getPayload().getStatusCode());
        assertEquals("Authentication failed: Missing or invalid JWT token.", response.getPayload().getStatusMessage());
    }
}
