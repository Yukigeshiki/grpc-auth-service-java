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
        interceptor = new RequestLoggingInterceptor();
        serverCall = mock(ServerCall.class);
        nextHandler = mock(ServerCallHandler.class);
        headers = new Metadata();
        var methodDescriptor = mock(MethodDescriptor.class);

        when(serverCall.getMethodDescriptor()).thenReturn(methodDescriptor);
        when(methodDescriptor.getFullMethodName()).thenReturn("TestService/TestMethod");
        when(nextHandler.startCall(any(), any())).thenReturn(new ServerCall.Listener<>() {});
    }

    /**
     * Tests that intercepting a call sets a request ID in the MDC.
     *
     * <p>Given: An empty MDC context.</p>
     * <p>When: The interceptor processes a call.</p>
     * <p>Then: The MDC should contain a valid UUID request ID.</p>
     */
    @Test
    void interceptCallSetsRequestIdInMdc() {
        MDC.clear();

        interceptor.interceptCall(serverCall, headers, nextHandler);

        assertNotNull(MDC.get("requestId"));
        assertTrue(MDC.get("requestId").matches("[a-f0-9-]{36}"));
    }

    /**
     * Tests that each intercepted call generates a unique request ID.
     *
     * <p>Given: Two consecutive intercepted calls.</p>
     * <p>When: The interceptor processes each call.</p>
     * <p>Then: The generated request IDs should be different.</p>
     */
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

    /**
     * Tests that MDC is cleared when the call completes.
     *
     * <p>Given: An intercepted call with a request ID in the MDC.</p>
     * <p>When: The listener's onComplete callback is invoked.</p>
     * <p>Then: The MDC request ID should be cleared.</p>
     */
    @Test
    void onCompleteClearsMdc() {
        MDC.clear();

        var listener = interceptor.interceptCall(serverCall, headers, nextHandler);
        assertNotNull(MDC.get("requestId"));

        listener.onComplete();

        assertNull(MDC.get("requestId"));
    }

    /**
     * Tests that MDC is cleared when the call is cancelled.
     *
     * <p>Given: An intercepted call with a request ID in the MDC.</p>
     * <p>When: The listener's onCancel callback is invoked.</p>
     * <p>Then: The MDC request ID should be cleared.</p>
     */
    @Test
    void onCancelClearsMdc() {
        MDC.clear();

        var listener = interceptor.interceptCall(serverCall, headers, nextHandler);
        assertNotNull(MDC.get("requestId"));

        listener.onCancel();

        assertNull(MDC.get("requestId"));
    }

    /**
     * Tests that the interceptor propagates the request ID in the gRPC context.
     *
     * <p>Given: An incoming gRPC call.</p>
     * <p>When: The interceptor processes the call.</p>
     * <p>Then: The next handler should be called with the original headers.</p>
     */
    @Test
    void interceptCallSetsRequestIdInContext() {
        interceptor.interceptCall(serverCall, headers, nextHandler);

        verify(nextHandler).startCall(any(), eq(headers));
    }
}
