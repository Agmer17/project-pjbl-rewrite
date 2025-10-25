package app.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import app.model.entity.ProductImage;

public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {
    
    
}
