package io.robothouse.grpcauth.service;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import io.robothouse.grpcauth.lib.util.JwtConstants;
import io.robothouse.grpcauth.proto.TokenServiceGrpc;
import io.robothouse.grpcauth.proto.ValidateTokenResponse;
import org.springframework.grpc.server.service.GrpcService;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

@GrpcService
public class TokenServiceImpl extends TokenServiceGrpc.TokenServiceImplBase {

    @Override
    public void validateToken(Empty request, StreamObserver<ValidateTokenResponse> responseObserver) {
        Optional<Jwt> jwtOptional = JwtConstants.JWT_CONTEXT_KEY.get();
        ValidateTokenResponse.Builder responseBuilder = ValidateTokenResponse.newBuilder();

        if (jwtOptional.isPresent()) {
            responseBuilder.setSuccess(true)
                    .setStatusCode(0)
                    .setStatusMessage("Authentication successful.");
        } else {
            responseBuilder.setSuccess(false)
                    .setStatusCode(16)
                    .setStatusMessage("Authentication failed: Missing or invalid JWT token.");
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }
}
