package com.reactive.webflux.api.restful.models.repository;

import com.practice.springboot.webflux.models.documents.Category;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CategoryRepository extends ReactiveMongoRepository<Category, String> { }
