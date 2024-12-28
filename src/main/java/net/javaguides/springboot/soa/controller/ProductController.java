// filepath: /C:/Users/azizb/Desktop/soa/src/main/java/net/javaguides/springboot/soa/controller/ProductController.java
package net.javaguides.springboot.soa.controller;

import net.javaguides.springboot.soa.entity.Product;
import net.javaguides.springboot.soa.service.ProductService;
import org.slf4j.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {
    private final ProductService service;
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAll(
            @RequestParam(defaultValue = "0") int page, 
            @RequestParam(defaultValue = "10") int size) {
        logger.info("Fetching all products - Page: {}, Size: {}", page, size);
        return ResponseEntity.ok(service.findAllPaginated(page, size));
    }

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product product) {
        logger.info("Creating product: {}", product.getName());
        return ResponseEntity.ok(service.save(product));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable String id) {
        logger.info("Fetching product by ID: {}", id);
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable String id, @RequestBody Product product) {
        logger.info("Updating product ID: {}", id);
        product.setId(id);
        return ResponseEntity.ok(service.save(product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        logger.info("Deleting product ID: {}", id);
        service.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getByCategory(@PathVariable String category) {
        logger.info("Fetching products by category: {}", category);
        return ResponseEntity.ok(service.findByCategory(category));
    }

    @GetMapping("/price")
    public ResponseEntity<List<Product>> getByPriceRange(
            @RequestParam Double min, 
            @RequestParam Double max) {
        logger.info("Fetching products with price between {} and {}", min, max);
        return ResponseEntity.ok(service.findByPriceBetween(min, max));
    }
}