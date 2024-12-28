package net.javaguides.springboot.soa.service;

import net.javaguides.springboot.soa.entity.Product;
import net.javaguides.springboot.soa.repository.ProductRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public List<Product> findAll() {
        return repository.findAll();
    }

    public List<Product> findAllPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = repository.findAll(pageable);
        return productPage.getContent();
    }

    public Optional<Product> findById(String id) {
        return repository.findById(id);
    }

    public Product save(Product product) {
        if (product.getId() != null) {
            product.setUpdatedAt(LocalDateTime.now());
        }
        return repository.save(product);
    }

    public void deleteById(String id) {
        repository.deleteById(id);
    }

    public List<Product> findByCategory(String category) {
        return repository.findByCategory(category);
    }

    public List<Product> findByPriceBetween(Double minPrice, Double maxPrice) {
        return repository.findByPriceBetween(minPrice, maxPrice);
    }
}