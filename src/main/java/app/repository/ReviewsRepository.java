package app.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import app.model.custom.ReviewStatus;
import app.model.dto.ReviewData;
import app.model.entity.Product;
import app.model.entity.Reviews;

@Repository
public interface ReviewsRepository extends JpaRepository<Reviews, UUID> {

    List<Reviews> findAllByProduct(Product product);

    List<Reviews> findAllByProductAndStatus(Product product, ReviewStatus status);

    List<Reviews> findAllByStatus(ReviewStatus status);

    @Query("""
                SELECT r
                FROM Reviews r
                JOIN FETCH r.user u
                JOIN FETCH r.product p
                LEFT JOIN FETCH p.images
                WHERE r.status = 'PENDING'
            """)
    List<ReviewData> findAllPendingReviewCards();

    @Query("""
                SELECT r
                FROM Reviews r
                JOIN FETCH r.user u
                JOIN FETCH r.product p
                LEFT JOIN FETCH p.images
                WHERE r.status = 'ACCEPTED'
            """)
    List<ReviewData> findAllReviewData();

}
