package app.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
                MIN(CASE WHEN pi.imageOrder = 1 THEN pi.imageFileName END) AS thumbnailUrl,
                COUNT(pi.id) AS imageCount
            FROM Product p
            LEFT JOIN p.category c
            LEFT JOIN p.images pi
            GROUP BY p.id, p.name, p.desc, p.price, c.name, p.createdAt
            """)
    Page<ProductProjection> findAllProductsForDashboard(Pageable pageable);

    @Query("""
            SELECT
                p.id AS id,
                p.name AS name,
                p.desc AS desc,
                p.price AS price,
                c.id AS categoryId,
                c.name AS categoryName,
                p.createdAt AS createdAt,
                MIN(CASE WHEN pi.imageOrder = 1 THEN pi.imageFileName END) AS thumbnailUrl,
                COUNT(pi.id) AS imageCount
            FROM Product p
            LEFT JOIN p.category c
            LEFT JOIN p.images pi
            WHERE (:categoryId IS NULL OR c.id = :categoryId)
              AND (:searchTerm IS NULL OR
                   p.name ILIKE CONCAT('%', :searchTerm, '%') OR
                   p.desc ILIKE CONCAT('%', :searchTerm, '%')
                  )
            GROUP BY p.id, p.name, p.desc, p.price, c.id, c.name, p.createdAt
            """)
    Page<ProductProjection> findAllProductsForDashboard(
            @Param("categoryId") UUID categoryId,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);

    @Query(value = """
            SELECT
                (SELECT COUNT(*) FROM product) AS totalProducts,
                (SELECT COUNT(*) FROM product_category) AS totalCategories,
                (SELECT COUNT(*) FROM product_image) AS totalImages
            """, nativeQuery = true)
    DashboardStatsProjection getDashboardStats();

    @Query("""
            SELECT p FROM Product p
            LEFT JOIN FETCH p.images
            LEFT JOIN FETCH p.category
            WHERE p.id = :id
                    """)
    Optional<Product> findDetailById(@Param("id") UUID id);

    @Query("""
               SELECT
                p.id AS id,
                p.name AS name,
                p.desc AS desc,
                p.price AS price,
                c.name AS categoryName,
                p.createdAt AS createdAt,
                MIN(CASE WHEN pi.imageOrder = 1 THEN pi.imageFileName END) AS thumbnailUrl,
                COUNT(pi.id) AS imageCount
            FROM Product p
            LEFT JOIN p.category c
            LEFT JOIN p.images pi
            GROUP BY p.id, p.name, p.desc, p.price, c.name, p.createdAt
            """)
    List<ProductProjection> findAllProductPreview();

}
