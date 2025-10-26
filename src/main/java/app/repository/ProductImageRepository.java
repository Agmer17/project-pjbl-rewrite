package app.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import app.model.entity.Product;
import app.model.entity.ProductImage;

public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {
    
    List<ProductImage> findAllByProduct(Product product);
}
