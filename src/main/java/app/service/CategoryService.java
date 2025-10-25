package app.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.model.dto.ProductCategoryPostDto;
import app.model.entity.ProductCategory;
import app.repository.ProductCategoryRepo;

@Service
public class CategoryService {

    @Autowired
    private ProductCategoryRepo repo;

    public List<ProductCategory> getAllCategory() {
        return repo.findAll();
    }

    public void saveCategory(ProductCategoryPostDto newCategory) {

        ProductCategory category = ProductCategory
        .builder()
        .name(newCategory.getName())
        .description(newCategory.getDesc()).build();

        repo.save(category);
    }

    public ProductCategory getById(UUID id) {

        // nanti ganti erro handling nya
        
        return repo.findById(id).orElseThrow(() -> new RuntimeException("kategori gak ada"));
    }
    
}
