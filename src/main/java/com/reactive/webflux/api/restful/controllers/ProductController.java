package com.reactive.webflux.api.restful.controllers;

import com.reactive.webflux.api.restful.models.documents.Product;
import com.reactive.webflux.api.restful.services.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Date;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public Mono<ResponseEntity<Flux<Product>>> getProducts() {
        // return Mono.just(ResponseEntity.ok(productService.findAll()));
        return Mono.just(
                ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(productService.findAll())
        );
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Product>> getProductDetail(@PathVariable String id) {
        // return Mono.just(ResponseEntity.ok(productService.findById(id)));
        return productService.findById(id)
                .map(product ->
                        ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(product))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Product>> createProduct(@RequestBody Product product) {
        if (product.getCreateAt() == null) {
            product.setCreateAt(new Date());
        }

        return productService.save(product)
                .map(product1 ->
                        ResponseEntity.created(URI.create("/api/products/".concat(product1.getId())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(product1)
                );
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Product>> editProduct(@RequestBody Product product, @PathVariable String id) {
        return productService.findById(id).flatMap(product1 -> {
                    product1.setName(product.getName());
                    product1.setPrice(product.getPrice());
                    product1.setCategory(product.getCategory());
                    return productService.save(product1);
                })
                .map(product1 ->
                        ResponseEntity.created(URI.create("/api/products/".concat(product1.getId())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(product1)
                )
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
