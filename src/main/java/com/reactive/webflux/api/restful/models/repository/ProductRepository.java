package com.reactive.webflux.api.restful.models.repository;

import com.reactive.webflux.api.restful.models.documents.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ProductRepository extends ReactiveMongoRepository<Product, String> { }
