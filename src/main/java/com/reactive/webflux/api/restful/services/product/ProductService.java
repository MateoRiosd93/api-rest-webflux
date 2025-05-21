package com.reactive.webflux.api.restful.services.product;

import com.reactive.webflux.api.restful.models.documents.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductService {
    Flux<Product> findAll();
    Flux<Product> findAllNameToUpperCase();
    Mono<Product> findByName(String name);
    Mono<Product> findById(String id);
    Mono<Product> save(Product product);
    Mono<Void> delete(Product product);
}
