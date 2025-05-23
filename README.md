# grpc-auth-service-java

[![build](https://github.com/Yukigeshiki/grpc-auth-service-java/actions/workflows/ci.yml/badge.svg)](https://github.com/Yukigeshiki/grpc-auth-service-java/actions/workflows/ci.yml)

An experimental gRPC authentication service using Spring Boot and GCP Identity Platform.

To start the application:
```
./gradlew clean bootRun
```

A JWT token is sent through to the service as an "Authorization: Bearer token" metadata pair. The service decodes the token and responds with success true/false, along with a status code and message.

To test you can set up simple email/password auth in [Identity Platform](https://cloud.google.com/identity-platform/docs), then run the below curl command to get a token. Also remember to set the `PROJECT_ID` (in the `application.yml` file) env variable to your GCP project ID.

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