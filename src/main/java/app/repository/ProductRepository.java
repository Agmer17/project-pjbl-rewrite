package app.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import app.model.entity.Product;
import app.model.entity.ProductProjection;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    @Query("""
                SELECT
                    p.id as id,
                    p.name as name,
                    p.desc as desc,
                    p.price as price,
                    c.name as categoryName,
                    p.createdAt as createdAt,
                    MIN(CASE WHEN pi.galleryImage = false THEN pi.imageFileName END) as thumbnailUrl,
                    COUNT(pi.id) as imageCount
                FROM Product p
                LEFT JOIN p.category c
                LEFT JOIN ProductImage pi ON pi.productId = p
                GROUP BY p.id, p.name, p.desc, p.price, c.name, p.createdAt
                ORDER BY p.createdAt DESC
            """)
    Page<ProductProjection> findAllProductsForDashboard(Pageable pageable);

    // Untuk stats
    @Query("SELECT COUNT(p) FROM Product p")
    Long countTotalProducts();

    @Query("SELECT COUNT(DISTINCT c) FROM ProductCategory c")
    Long countTotalCategories();

    @Query("SELECT COUNT(pi) FROM ProductImage pi")
    Long countTotalImages();

}
