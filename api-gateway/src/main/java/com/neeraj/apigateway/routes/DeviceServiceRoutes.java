package com.neeraj.apigateway.routes;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

@Configuration
public class DeviceServiceRoutes {

    @Bean
    public RouterFunction<ServerResponse> deviceRoute() {
        return route("device-service") // Logical name for the route
                .route(RequestPredicates.path("/api/v1/device/**"), http()) // Match all paths under /api/v1/device/ and use HTTP methods (GET, POST, etc.)
                .before(uri("http://localhost:8081")) // Forward the request to the device service running on localhost:8081
                .build();
    }
}
