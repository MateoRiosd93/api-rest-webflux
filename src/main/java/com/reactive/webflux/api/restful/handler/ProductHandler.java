package com.reactive.webflux.api.restful.handler;

import com.reactive.webflux.api.restful.models.documents.Product;
import com.reactive.webflux.api.restful.services.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Date;

@RequiredArgsConstructor
@Component
public class ProductHandler {

    private final ProductService productService;

    public Mono<ServerResponse> getProducts(ServerRequest serverRequest){
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(productService.findAll(), Product.class);
    }

    public Mono<ServerResponse> getProductDetail(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");

        return productService.findById(id).flatMap(product ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(product)
                )
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> createProduct(ServerRequest serverRequest) {
        Mono<Product> productMono = serverRequest.bodyToMono(Product.class);

        return productMono.flatMap(product -> {
                    if (product.getCreateAt() == null) {
                        product.setCreateAt(new Date());
                    }

                    return productService.save(product);
                })
                .flatMap(product ->
                        ServerResponse.created(URI.create("/api/hanlder/products" + product.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(product)
                );
    }

    public Mono<ServerResponse> editProduct(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");
        Mono<Product> productMono = serverRequest.bodyToMono(Product.class);

        Mono<Product> productDB = productService.findById(id);

        return productDB.zipWith(productMono, (dbProduct, requestProduct) -> {
                    dbProduct.setName(requestProduct.getName());
                    dbProduct.setPrice(requestProduct.getPrice());
                    dbProduct.setCategory(requestProduct.getCategory());

                    return dbProduct;
                })
                .flatMap(editedProduct -> ServerResponse
                        .created(URI.create("/api/handler/products/".concat(editedProduct.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(productService.save(editedProduct), Product.class))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> deleteProduct(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");

        return productService.findById(id)
                .flatMap(product -> productService.delete(product).then(ServerResponse.noContent().build()))
                .switchIfEmpty(ServerResponse.notFound().build());
    }
}
