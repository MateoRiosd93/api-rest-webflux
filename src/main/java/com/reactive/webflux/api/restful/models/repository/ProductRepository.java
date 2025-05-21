package com.reactive.webflux.api.restful.models.repository;

import com.reactive.webflux.api.restful.models.documents.Product;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ProductRepository extends ReactiveMongoRepository<Product, String> {
    // Esto seria usando las palabras claves para realizar una consulta por nombre palabra clave findBy y luego
    // la propiedad por la cual vamos a hacer la busqueda findByName
    Mono<Product> findByName(String name);

    // Ahora la misma consulta pero utilizando Query de mongo
    @Query("{ 'name': ?0 }")
    Mono<Product> getByName(String name);

}
