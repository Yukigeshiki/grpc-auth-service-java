# grpc-auth-service-java

An experimental gRPC authentication service using Spring Boot and GCP Identity Platform.

To start the application:
```
./gradlew clean bootRun
```

A JWT token is then sent through to the service as an "Authorization: Bearer <jwt-token>" metadata pair. The service decodes the token and determines whether it is valid or not.

To test you can set up simple email/password auth in [Identity Platform](https://cloud.google.com/identity-platform/docs), then run the below curl command to get a token.

```
curl -X POST "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=<your-api-key>" \
  -H "Content-Type: application/json" \
  -d '{
        "email": "<your-email>",
        "password": "<your-password>",
        "returnSecureToken": true
      }'
```
