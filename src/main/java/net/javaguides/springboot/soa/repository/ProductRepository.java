package net.javaguides.springboot.soa.repository;

import net.javaguides.springboot.soa.entity.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String>, PagingAndSortingRepository<Product, String> {
    List<Product> findByCategory(String category);
    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);
}