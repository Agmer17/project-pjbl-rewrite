package app.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import app.model.entity.Product;
import app.model.entity.ProductImage;
import jakarta.transaction.Transactional;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {

    List<ProductImage> findAllByProduct(Product product);

    @Modifying
    @Transactional
    @Query("DELETE FROM ProductImage i WHERE i.id IN :ids")
    void deleteAllByIdIn(@Param("ids") List<UUID> ids);

    @Query("SELECT i FROM ProductImage i WHERE i.id IN :ids")
    List<ProductImage> findAllByIdIn(@Param("ids") List<UUID> ids);

    List<ProductImage> findAllByGalleryImageTrue();

    List<ProductImage> findAllByGalleryImageFalse();

    @Query(value = "select * from product_image ORDER BY RANDOM() LIMIT :n", nativeQuery = true)
    List<ProductImage> findRandomImages(@Param("n") Integer n);
}


