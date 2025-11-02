package app.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import app.model.dto.ProductCategoryPostDto;
import app.model.entity.ProductCategory;
import app.repository.ProductCategoryRepo;
import jakarta.transaction.Transactional;

@Service
public class CategoryService {

    @Autowired
    private ProductCategoryRepo repo;

    public List<ProductCategory> getAllCategory() {
        return repo.findAll();
    }

    public ResponseEntity<ProductCategory> saveCategory(ProductCategoryPostDto newCategory) {

        ProductCategory category = ProductCategory
        .builder()
        .name(newCategory.getName())
        .description(newCategory.getDesc()).build();

        repo.save(category);

        return ResponseEntity.ok().body(category);
    }

    public ProductCategory getById(UUID id) {

        // nanti ganti erro handling nya
        
        return repo.findById(id).orElseThrow(() -> new RuntimeException("kategori gak ada"));
    }

    @Transactional
    public ResponseEntity<ProductCategory> editCategory(ProductCategoryPostDto editRequest, UUID id) {
        
        ProductCategory existingOne = repo.findById(id).get();

        if (existingOne == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        
        
        if (!existingOne.getName().equals(editRequest.getName())) {
            
            if (repo.existsByName(editRequest.getName())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
            }
            existingOne.setName(editRequest.getName());

        }
        
        existingOne.setDescription(editRequest.getDesc());

        return ResponseEntity.ok().body(existingOne);
    }

    public ResponseEntity<?> deletecategory(UUID id) {

         try {
            if (!repo.existsById(id)) {
                
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Kategori tidak ditemukan"));
            }
            ProductCategory category = repo.findById(id).get();
                category.getProducts().forEach(p -> p.setCategory(null));
            repo.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Kategori berhasil dihapus"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Terjadi kesalahan saat menghapus kategori"));
        }

    }
    
}
