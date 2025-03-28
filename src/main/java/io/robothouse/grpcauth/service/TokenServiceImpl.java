package io.robothouse.grpcauth.service;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import io.robothouse.grpcauth.proto.TokenServiceGrpc;
import io.robothouse.grpcauth.proto.ValidateTokenResponse;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
public class TokenServiceImpl extends TokenServiceGrpc.TokenServiceImplBase {

    @Override
    public void validateToken(Empty request, StreamObserver<ValidateTokenResponse> responseObserver) {
        ValidateTokenResponse response = ValidateTokenResponse.newBuilder()
                .setValid(true)
                .setMessage("Successfully authenticated.")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
