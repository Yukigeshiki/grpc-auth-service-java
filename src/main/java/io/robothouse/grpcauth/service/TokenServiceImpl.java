package io.robothouse.grpcauth.service;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import io.robothouse.grpcauth.lib.util.CtxConstants;
import io.robothouse.grpcauth.proto.TokenResponsePayload;
import io.robothouse.grpcauth.proto.TokenServiceGrpc;
import io.robothouse.grpcauth.proto.ValidateTokenResponse;
import org.springframework.grpc.server.service.GrpcService;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@GrpcService
public class TokenServiceImpl extends TokenServiceGrpc.TokenServiceImplBase {

    @Override
    public void validateToken(Empty request, StreamObserver<ValidateTokenResponse> responseObserver) {
        String timestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String requestId = CtxConstants.REQUEST_ID.get();

        TokenResponsePayload.Builder payloadBuilder = TokenResponsePayload.newBuilder();
        if (CtxConstants.JWT_CONTEXT_KEY.get().isPresent()) {
            payloadBuilder.setSuccess(true)
                    .setStatusCode(0)
                    .setStatusMessage("Authentication successful.");
        } else {
            payloadBuilder.setSuccess(false)
                    .setStatusCode(16)
                    .setStatusMessage("Authentication failed: Missing or invalid JWT token.");
        }

        ValidateTokenResponse response = ValidateTokenResponse.newBuilder()
                .setRequestId(requestId)
                .setDatetime(timestamp)
                .setPayload(payloadBuilder)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
