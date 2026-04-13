package com.neeraj.apigateway.routes;

import org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions.setPath;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

@Configuration
public class DeviceServiceRoutes {

    /**
     * This method defines a route for the device service. It matches all requests to paths under /api/v1/device/** and forwards them to the device service running on localhost:8081.
     * It also applies a circuit breaker filter that will forward requests to a fallback route if the device service is unavailable.
     */
    @Bean
    public RouterFunction<ServerResponse> deviceRoute() {
        return route("device-service") // Logical name for the route
                .route(RequestPredicates.path("/api/v1/device/**"), http()) // Match all paths under /api/v1/device/ and use HTTP methods (GET, POST, etc.)
                .before(uri("http://localhost:8081")) // Forward the request to the device service running on localhost:8081
                .filter(CircuitBreakerFilterFunctions.circuitBreaker(
                        "device-service-circuit-breaker",
                        URI.create("forward:/fallbackRoute")
                )) // Add a circuit breaker filter that forwards to /fallbackRoute if the device service is unavailable
                .build();
    }

    /**
     * This method defines a fallback route for the device service. If the device service is unavailable, requests will be forwarded to this route, which returns a 503 Service Unavailable response with a message.
     */
    @Bean
    public RouterFunction<ServerResponse> deviceFallbackRoute() {
        return route("device-service-fallback")
                .route(RequestPredicates.path("/fallbackRoute"),
                        req -> ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                                .body("Device Service is currently unavailable. Please try again later."))
                .build();
    }

    /**
     * This method defines a route for the device service API documentation. It matches requests to /docs/device-service/v3/api-docs and forwards them to the device service's API docs endpoint.
     */
    @Bean
    public RouterFunction<ServerResponse> deviceServiceApiDocsRoute() {
        return route("device-service-api-docs")
                .route(RequestPredicates.path("/docs/device-service/v3/api-docs"), http())
                .before(uri("http://localhost:8081"))
                .filter(setPath("/v3/api-docs"))
                .build();
    }
}
