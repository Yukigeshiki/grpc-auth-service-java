package io.robothouse.grpcauth.lib.component;

import io.grpc.*;
import io.robothouse.grpcauth.lib.util.CtxConstants;
import lombok.extern.log4j.Log4j2;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * gRPC server interceptor that provides request logging and distributed tracing capabilities.
 */
@Log4j2
@Component
@GlobalServerInterceptor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingInterceptor implements ServerInterceptor {

    /**
     * MDC key for the unique request identifier used in log entries.
     */
    private static final String MDC_REQUEST_ID_KEY = "requestId";

    /**
     * Intercepts incoming gRPC calls to add logging and request tracking.
     */
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next
    ) {
        var methodName = call.getMethodDescriptor().getFullMethodName();
        var requestId = UUID.randomUUID().toString();

        MDC.put(MDC_REQUEST_ID_KEY, requestId);
        log.info("Incoming gRPC request for method: {}", methodName);

        var ctx = Context.current().withValue(CtxConstants.REQUEST_ID_CONTEXT_KEY, requestId);
        var listener = Contexts.interceptCall(ctx, call, headers, next);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(listener) {
            @Override
            public void onComplete() {
                try {
                    super.onComplete();
                } finally {
                    MDC.remove(MDC_REQUEST_ID_KEY);
                }
            }

            @Override
            public void onCancel() {
                try {
                    super.onCancel();
                } finally {
                    MDC.remove(MDC_REQUEST_ID_KEY);
                }
            }
        };
    }
}
