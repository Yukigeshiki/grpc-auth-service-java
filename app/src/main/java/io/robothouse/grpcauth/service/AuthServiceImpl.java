package io.robothouse.grpcauth.service;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.robothouse.grpcauth.lib.util.CtxConstants;
import io.robothouse.grpcauth.proto.AuthPayload;
import io.robothouse.grpcauth.proto.AuthResponse;
import io.robothouse.grpcauth.proto.AuthServiceGrpc;
import lombok.extern.log4j.Log4j2;
import org.springframework.grpc.server.service.GrpcService;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * gRPC service implementation for authentication operations.
 *
 * <p>Returns an AuthResponse on successful authentication, or throws a gRPC
 * StatusException with UNAUTHENTICATED status on failure.</p>
 */
@Log4j2
@GrpcService
public class AuthServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {

    /**
     * Handles authentication requests by validating the JWT token from the context.
     *
     * <p>On success, returns an AuthResponse with the authentication details.
     * On failure, throws a gRPC UNAUTHENTICATED status exception.</p>
     */
    @Override
    public void authenticate(Empty request, StreamObserver<AuthResponse> responseObserver) {
        var timestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        var requestId = CtxConstants.REQUEST_ID_CONTEXT_KEY.get();
        if (requestId == null) {
            requestId = "unknown";
            log.warn("Request ID not found in context");
        }
        var jwtOptional = CtxConstants.JWT_CONTEXT_KEY.get();

        if (jwtOptional.isEmpty()) {
            responseObserver.onError(Status.UNAUTHENTICATED
                    .withDescription("Missing or invalid JWT token")
                    .asRuntimeException());
            return;
        }

        var payload = AuthPayload.newBuilder()
                .setSuccess(true)
                .setStatusCode(Status.OK.getCode().value())
                .setStatusMessage("Authentication successful.")
                .build();

        var response = AuthResponse.newBuilder()
                .setRequestId(requestId)
                .setDatetime(timestamp)
                .setPayload(payload)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
