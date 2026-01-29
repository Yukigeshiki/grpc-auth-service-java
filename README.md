# grpc-auth-service-java

[![build](https://github.com/Yukigeshiki/grpc-auth-service-java/actions/workflows/ci.yml/badge.svg)](https://github.com/Yukigeshiki/grpc-auth-service-java/actions/workflows/ci.yml)

An experimental gRPC authentication service using Spring Boot and GCP Identity Platform.

## Prerequisites

- Java 23
- [grpcurl](https://github.com/fullstorydev/grpcurl) (for testing)

## Running the Application
```
./gradlew clean bootRun
```

A JWT token is sent through to the service as an "Authorization: Bearer token" metadata pair. The service decodes the token and returns an `AuthResponse` on success, or a gRPC `UNAUTHENTICATED` error on failure.

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `PROJECT_ID` | `grpc-identity-platform-test` | GCP project ID for Firebase auth |
| `ISSUER_URI` | `https://securetoken.google.com/` | JWT issuer base URI |
| `GRPC_REFLECTION_ENABLED` | `false` | Enable gRPC reflection (disable in production) |

## Testing

To test you can set up simple email/password auth in [Identity Platform](https://cloud.google.com/identity-platform/docs), then run the below curl command to get a token. Remember to set the `PROJECT_ID` environment variable to your GCP project ID.

```
curl -X POST "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=<your-api-key>" \
  -H "Content-Type: application/json" \
  -d '{
        "email": "<your-email>",
        "password": "<your-password>",
        "returnSecureToken": true
      }'
```

Once the above is completed and you have a token, run: 

```
grpcurl -plaintext \
  -emit-defaults \
  -d '{}' \
  -H "Authorization: Bearer <jwt-token>" \
  localhost:9090 AuthService/Authenticate
```

To run auth service tests:
```
./gradlew clean check
```