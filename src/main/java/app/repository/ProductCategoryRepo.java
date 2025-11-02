package app.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import app.model.entity.ProductCategory;

public interface ProductCategoryRepo extends JpaRepository<ProductCategory, UUID> {

    Boolean existsByName(String name);

    
    
}
