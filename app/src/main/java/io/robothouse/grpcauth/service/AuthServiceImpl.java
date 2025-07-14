package io.robothouse.grpcauth.service;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import io.robothouse.grpcauth.lib.util.CtxConstants;
import io.robothouse.grpcauth.proto.AuthPayload;
import io.robothouse.grpcauth.proto.AuthResponse;
import io.robothouse.grpcauth.proto.AuthServiceGrpc;
import lombok.extern.log4j.Log4j2;
import org.springframework.grpc.server.service.GrpcService;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Log4j2
@GrpcService
public class AuthServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {

    @Override
    public void authenticate(Empty request, StreamObserver<AuthResponse> responseObserver) {
        var timestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        var requestId = CtxConstants.REQUEST_ID_CONTEXT_KEY.get();

        var payloadBuilder = CtxConstants.JWT_CONTEXT_KEY.get().isPresent() ?
                AuthPayload.newBuilder()
                        .setSuccess(true)
                        .setStatusCode(0)
                        .setStatusMessage("Authentication successful.") :
                AuthPayload.newBuilder()
                        .setSuccess(false)
                        .setStatusCode(16)
                        .setStatusMessage("Authentication failed: Missing or invalid JWT token.");

        var response = AuthResponse.newBuilder()
                .setRequestId(requestId)
                .setDatetime(timestamp)
                .setPayload(payloadBuilder)
                .build();

        log.info("Response: {}", response);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
