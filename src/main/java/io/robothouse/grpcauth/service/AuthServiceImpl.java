package io.robothouse.grpcauth.service;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import io.robothouse.grpcauth.lib.util.CtxConstants;
import io.robothouse.grpcauth.proto.AuthPayload;
import io.robothouse.grpcauth.proto.AuthResponse;
import io.robothouse.grpcauth.proto.AuthServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.grpc.server.service.GrpcService;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@GrpcService
public class AuthServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Override
    public void authenticate(Empty request, StreamObserver<AuthResponse> responseObserver) {
        String timestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String requestId = CtxConstants.REQUEST_ID_CONTEXT_KEY.get();

        AuthPayload.Builder payloadBuilder = AuthPayload.newBuilder();
        if (CtxConstants.JWT_CONTEXT_KEY.get().isPresent()) {
            payloadBuilder.setSuccess(true)
                    .setStatusCode(0)
                    .setStatusMessage("Authentication successful.");
        } else {
            payloadBuilder.setSuccess(false)
                    .setStatusCode(16)
                    .setStatusMessage("Authentication failed: Missing or invalid JWT token.");
        }

        AuthResponse response = AuthResponse.newBuilder()
                .setRequestId(requestId)
                .setDatetime(timestamp)
                .setPayload(payloadBuilder)
                .build();

        log.info("Response: {}", response);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
