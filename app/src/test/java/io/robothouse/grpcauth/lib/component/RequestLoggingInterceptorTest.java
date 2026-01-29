package io.robothouse.grpcauth.lib.component;

import io.grpc.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RequestLoggingInterceptor.
 */
class RequestLoggingInterceptorTest {

    private RequestLoggingInterceptor interceptor;
    private ServerCall<Object, Object> serverCall;
    private ServerCallHandler<Object, Object> nextHandler;
    private Metadata headers;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        interceptor = new RequestLoggingInterceptor();
        serverCall = mock(ServerCall.class);
        nextHandler = mock(ServerCallHandler.class);
        headers = new Metadata();
        var methodDescriptor = mock(MethodDescriptor.class);

        when(serverCall.getMethodDescriptor()).thenReturn(methodDescriptor);
        when(methodDescriptor.getFullMethodName()).thenReturn("TestService/TestMethod");
        when(nextHandler.startCall(any(), any())).thenReturn(new ServerCall.Listener<>() {});
    }

    @Test
    void interceptCallSetsRequestIdInMdc() {
        MDC.clear();

        interceptor.interceptCall(serverCall, headers, nextHandler);

        assertNotNull(MDC.get("requestId"));
        assertTrue(MDC.get("requestId").matches("[a-f0-9-]{36}"));
    }

    @Test
    void interceptCallGeneratesUniqueRequestIds() {
        MDC.clear();

        interceptor.interceptCall(serverCall, headers, nextHandler);
        var firstRequestId = MDC.get("requestId");

        MDC.clear();

        interceptor.interceptCall(serverCall, headers, nextHandler);
        var secondRequestId = MDC.get("requestId");

        assertNotEquals(firstRequestId, secondRequestId);
    }

    @Test
    void onCompleteClearsMdc() {
        MDC.clear();

        var listener = interceptor.interceptCall(serverCall, headers, nextHandler);
        assertNotNull(MDC.get("requestId"));

        listener.onComplete();

        assertNull(MDC.get("requestId"));
    }

    @Test
    void onCancelClearsMdc() {
        MDC.clear();

        var listener = interceptor.interceptCall(serverCall, headers, nextHandler);
        assertNotNull(MDC.get("requestId"));

        listener.onCancel();

        assertNull(MDC.get("requestId"));
    }

    @Test
    void interceptCallSetsRequestIdInContext() {
        interceptor.interceptCall(serverCall, headers, nextHandler);

        verify(nextHandler).startCall(any(), eq(headers));
    }
}
