package com.reactive.webflux.api.restful.controllers;

import com.reactive.webflux.api.restful.models.documents.Product;
import com.reactive.webflux.api.restful.services.product.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;
    @Value("{config.uploads.path}")
    private String path;

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

    // Utilizando @Valid
    @PostMapping("/create/valid")
    public Mono<ResponseEntity<Map<String, Object>>> createProductWithValid(@Valid @RequestBody Mono<Product> productMono) {

        Map<String, Object> response = new HashMap<>();

        return productMono.flatMap(product -> {
            if (product.getCreateAt() == null) {
                product.setCreateAt(new Date());
            }

            return productService.save(product).map(product1 -> {
                response.put("product", product1);
                response.put("message", "Product create!!!");
                response.put("tiemstamp", new Date());

                return ResponseEntity
                        .created(URI.create("/api/products/create/valid".concat(product1.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);
            });
        }).onErrorResume(throwable -> {
            return Mono.just(throwable).cast(WebExchangeBindException.class)
                    .flatMap(error -> Mono.just(error.getFieldErrors()))
                    .flatMapMany(Flux::fromIterable)
                    .map(fieldError -> "Field" + fieldError.getField() + " " + fieldError.getDefaultMessage())
                    .collectList()
                    .flatMap(errors -> {
                        response.put("errors", errors);
                        response.put("tiemstamp", new Date());
                        response.put("status", HttpStatus.BAD_REQUEST.value());

                        return Mono.just(ResponseEntity.badRequest().body(response));
                    });
        });
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

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteProduct(@PathVariable String id) {
        return productService.findById(id)
                .flatMap(product -> productService.delete(product).then(Mono.just((new ResponseEntity<Void>(HttpStatus.NO_CONTENT)))))
                .defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
    }


    @PostMapping("/upload/{id}")
    public Mono<ResponseEntity<Product>> uploadImage(@PathVariable String id, @RequestPart FilePart filePart) {
        return productService.findById(id)
                .flatMap(product -> {
                    product.setImage(UUID.randomUUID().toString() + "-" + filePart.filename()
                            .replace(" ", "")
                            .replace(":", "")
                            .replace("\\", ""));

                    return filePart.transferTo(new File(path + product.getImage())).then(productService.save(product));
                })
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/create/v2")
    public Mono<ResponseEntity<Product>> createProductWithImage(Product product, @RequestPart FilePart filePart) {
        if (product.getCreateAt() == null) {
            product.setCreateAt(new Date());
        }

        product.setImage(UUID.randomUUID().toString() + "-" + filePart.filename()
                .replace(" ", "")
                .replace(":", "")
                .replace("\\", ""));


        return filePart.transferTo(new File(path + product.getImage()))
                .then(productService.save(product))
                .map(product1 ->
                        ResponseEntity.created(URI.create("/api/products/".concat(product1.getId())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(product1)
                );
    }
}
