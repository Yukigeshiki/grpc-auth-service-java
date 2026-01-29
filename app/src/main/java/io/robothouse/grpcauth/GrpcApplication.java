package io.robothouse.grpcauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the gRPC Authentication Service application.
 *
 * <p>This Spring Boot application provides a gRPC-based authentication service
 * that validates JWT tokens issued by Google Firebase.</p>
 *
 * <p>The application includes:</p>
 * <ul>
 *   <li>JWT token validation via Firebase's secure token endpoint</li>
 *   <li>Request logging with unique request ID generation for distributed tracing</li>
 *   <li>gRPC interceptors for authentication and logging</li>
 * </ul>
 */
@SpringBootApplication
public class GrpcApplication {
    public static void main(String[] args) {
        SpringApplication.run(GrpcApplication.class, args);
    }
}
