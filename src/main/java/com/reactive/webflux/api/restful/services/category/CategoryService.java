package com.reactive.webflux.api.restful.services.category;

import com.reactive.webflux.api.restful.models.documents.Category;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CategoryService {
    Flux<Category> findAll();
    Mono<Category> findById(String id);
    Mono<Category> save(Category category);
    Mono<Void> delete(Category category);
}
