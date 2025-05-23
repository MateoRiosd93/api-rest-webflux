package com.reactive.webflux.api.restful.handler;

import com.reactive.webflux.api.restful.models.documents.Product;
import com.reactive.webflux.api.restful.services.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class ProductHandler {

    @Value("${config.uploads.path}")
    private String path;

    private final ProductService productService;

    private final Validator validator;

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
            Errors errors = new BeanPropertyBindingResult(product, Product.class.getName());
            validator.validate(product, errors);

            if (errors.hasErrors()) {
                return Flux.fromIterable(errors.getFieldErrors()).map(fieldError -> "The field" + fieldError.getField() + " " + fieldError.getDefaultMessage())
                        .collectList()
                        .flatMap(listErrors -> ServerResponse
                                .badRequest()
                                .bodyValue(listErrors)
                        );
            } else {
                if (product.getCreateAt() == null) {
                    product.setCreateAt(new Date());
                }

                return productService.save(product)
                        .flatMap(product1 ->
                                ServerResponse.created(URI.create("/api/hanlder/products" + product1.getId()))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(product1)
                        );
            }
        });
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

    public Mono<ServerResponse> uploadImage(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");

        return serverRequest.multipartData()
                .map(stringPartMultiValueMap ->
                        stringPartMultiValueMap.toSingleValueMap().get("file"))
                .cast(FilePart.class)
                .flatMap(file -> productService.findById(id)
                        .flatMap(product -> {
                            product.setImage(UUID.randomUUID() + "-" + file.filename()
                                    .replace(" ", "")
                                    .replace(":", "")
                                    .replace("\\", "")
                            );

                            return file.transferTo(new File(path + product.getImage())).then(productService.save(product));
                        })).flatMap(product ->
                        ServerResponse.created(URI.create("/api/hanlder/products/upload"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(product)
                )
                .switchIfEmpty(ServerResponse.notFound().build());
    }
}
