package app.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import app.model.custom.ReviewStatus;
import app.model.dto.PostReviewRequest;
import app.model.dto.ReviewData;
import app.model.entity.Product;
import app.model.entity.Reviews;
import app.model.entity.Users;
import app.repository.ReviewsRepository;
import jakarta.transaction.Transactional;

@Service
public class ReviewsService {

    @Autowired
    private ReviewsRepository reviewRepo;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    public List<Reviews> getFromProduct(UUID id, String reds) {

        Product product = productService.getProductDetails(id, reds);

        List<Reviews> reviewsFromProduct = reviewRepo.findAllByProduct(product);

        return reviewsFromProduct;

    }

    public Map<Integer, Long> countRatings(List<Reviews> reviews) {
        return reviews.stream()
                .collect(Collectors.groupingBy(
                        Reviews::getRating,
                        Collectors.counting()));
    }

    public Reviews getById(UUID id) {
        return reviewRepo.findById(id).orElse(null);
    }

    public Double averageReviews(List<Reviews> reviews) {

        return reviews.stream().mapToInt(Reviews::getRating)
                .average().orElse(0.0);

    }

    public Boolean eligible(List<Reviews> reviews, UUID userId) {

        return !reviews.stream().anyMatch(r -> r.getUser().getId().equals(userId));

    }

    @Transactional
    public ResponseEntity<?> saveReviews(PostReviewRequest req, UUID userId) {

        Product product = productService.getProductDetails(req.getProductId());

        if (product == null) {

            return ResponseEntity.badRequest().body("Produk tidak ditemukan!");

        }

        Users user = userService.getUserById(userId);

        if (user == null) {

            return ResponseEntity.badRequest().body("User tidak ditemukan!");

        }

        List<Reviews> productReview = reviewRepo.findAllByProduct(product);

        Boolean everPostReview = productReview.stream().anyMatch(p -> p.getUser().getId().equals(user.getId()));

        if (everPostReview) {

            return ResponseEntity.status(HttpStatus.CONFLICT).body("kamu sudah pernah post review di product ini");

        }

        Reviews review = Reviews.builder()
                .user(user)
                .textReview(req.getTextReview())
                .product(product)
                .rating(req.getRating())
                .status(ReviewStatus.PENDING)
                .build();

        reviewRepo.save(review);

        return ResponseEntity.ok().body("Berhasil menambahkan review");

    }

    @Transactional
    public ResponseEntity<?> deleteReviews(UUID reviewId) {
        Reviews reviews = reviewRepo.findById(reviewId).orElse(null);

        if (reviews == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("review tidak ditemukan");
        }

        reviewRepo.delete(reviews);

        return ResponseEntity.ok().build();

    }

    @Transactional
    public ResponseEntity<?> acceptReview(UUID id) {

        Reviews reviews = reviewRepo.findById(id).orElse(null);

        if (reviews == null) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("review tidak ditemukan mungkin terhapus!");
        }

        reviews.setStatus(ReviewStatus.ACCEPTED);

        reviewRepo.save(reviews);

        return ResponseEntity.ok().body("berhasil mengacc review");

    }

    public List<ReviewData> getPendingReviews() {
        return reviewRepo.findAllPendingReviewCards();
    }

    public Boolean isReviewPending(UUID userId, List<Reviews> reviews) {
        boolean isPending = reviews.stream()
                .anyMatch(r -> r.getUser().getId().equals(userId) &&
                        r.getStatus() == ReviewStatus.PENDING);
        return isPending;
    }

    public List<ReviewData> getAllReviews() {
        return reviewRepo.findAllReviewData();
    }

}
