package com.reactive.webflux.api.restful;

import com.reactive.webflux.api.restful.handler.ProductHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

// Se puede importar de forma static para implementar el metodo route y todos los predicates.
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterFunctionConfig {

    @Bean
    public RouterFunction<ServerResponse> routes(ProductHandler productHandler) {
        return route()
                .GET("/api/handler/products", productHandler::getProducts)
                .GET("/api/handler/products/{id}", productHandler::getProductDetail)
                .POST("/api/handler/products", productHandler::createProduct)
                .PUT("/api/handler/products/{id}", productHandler::editProduct)
                .DELETE("/api/handler/products/{id}", productHandler::deleteProduct)
                .build();
    }
}
