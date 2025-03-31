package io.robothouse.grpcauth.lib.component;

import io.grpc.*;
import io.robothouse.grpcauth.lib.util.CtxConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@GlobalServerInterceptor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingInterceptor implements ServerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingInterceptor.class);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next
    ) {
        String methodName = call.getMethodDescriptor().getFullMethodName();
        String requestId = UUID.randomUUID().toString();

        MDC.put("requestId", requestId);
        log.info("Incoming gRPC request for method: {}", methodName);

        Context ctx = Context.current().withValue(CtxConstants.REQUEST_ID_CONTEXT_KEY, requestId);
        return Contexts.interceptCall(ctx, call, headers, next);
    }
}
