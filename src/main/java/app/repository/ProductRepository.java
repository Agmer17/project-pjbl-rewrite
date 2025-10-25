package app.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import app.model.entity.Product;
import app.model.entity.ProductProjection;
import app.model.projection.DashboardStatsProjection;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    @Query("""
            SELECT
                p.id AS id,
                p.name AS name,
                p.desc AS desc,
                p.price AS price,
                c.name AS categoryName,
                p.createdAt AS createdAt,
                MIN(CASE WHEN pi.galleryImage = false THEN pi.imageFileName END) AS thumbnailUrl,
                COUNT(pi.id) AS imageCount
            FROM Product p
            LEFT JOIN p.category c
            LEFT JOIN p.images pi
            GROUP BY p.id, p.name, p.desc, p.price, c.name, p.createdAt
            ORDER BY p.createdAt DESC
            """)
    Page<ProductProjection> findAllProductsForDashboard(Pageable pageable);

    @Query(value = """
            SELECT
                (SELECT COUNT(*) FROM product) AS totalProducts,
                (SELECT COUNT(*) FROM product_category) AS totalCategories,
                (SELECT COUNT(*) FROM product_image) AS totalImages
            """, nativeQuery = true)
    DashboardStatsProjection getDashboardStats();

}
