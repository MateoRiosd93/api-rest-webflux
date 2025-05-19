package com.reactive.webflux.api.restful.models.documents;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "products")
public class Product {
    @Id
    private String id;
    @NotEmpty
    private String name;
    @NotNull
    private Double price;
    // Agregamos esta anotacion ya que desde el front se manda con ese formato, y sin esto genera un error al hacer el mapeo a tipo Date.
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private Date createAt;
    private String image;
    // Se agrega atributo category para relacionar el producto con una categoria
    @Valid
    private Category category;


    public Product(String name, Double price){
        this.name = name;
        this.price = price;
    }

    public Product(String name, Double price, Category category){
        this(name, price);
        this.category = category;
    }
}
