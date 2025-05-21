package com.reactive.webflux.api.restful;

import com.reactive.webflux.api.restful.models.documents.Product;
import com.reactive.webflux.api.restful.services.product.ProductService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RestfulApplicationTests {

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private ProductService productService;

	@Test
	public void getProducts() {
		webTestClient.get()
				.uri("/api/handler/products")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBodyList(Product.class)
				// En caso de querer hacer assertions sobre la respuesta como tal tenemos el metodo consumeWith()
				.consumeWith(response -> {
					List<Product> productList = response.getResponseBody();

					Assertions.assertNotNull(productList);
				});
	}

	@Test
	public void getProductDetail() {
		Product product = productService.findByName("Play station 5").block();

		webTestClient.get()
				.uri("/api/handler/products/{id}", Collections.singletonMap("id", Objects.requireNonNull(product).getId()))
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody(Product.class)
				.consumeWith(response -> {
					Product product1 = response.getResponseBody();

                    if (product1 != null) {
                        org.assertj.core.api.Assertions.assertThat(product1.getId()).isNotEmpty();
						org.assertj.core.api.Assertions.assertThat(product1.getName()).isEqualTo("Play station 5");
                    }


				});
				// Esto seria una forma de validar sin tener la respuesta como tipo Product
				//.jsonPath("$.id").isNotEmpty()
				//.jsonPath("$.name").isEqualTo("Play station 5");
	}

}
