package net.javaguides.springboot.soa.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.*; // Updated import
import java.time.LocalDateTime;

@Document(collection = "products")
public class Product {
    @Id
    private String id;

    @NotBlank(message = "Name is mandatory")
    private String name;

    @NotNull(message = "Price is mandatory")
    @Positive(message = "Price must be positive")
    private Double price;

    private String description;

    @NotNull(message = "Stock is mandatory")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    @NotBlank(message = "Category is mandatory")
    private String category;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Product() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Product(String name, Double price, String description) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters...

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) {
        if (price != null && price <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        this.price = price;
    }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}