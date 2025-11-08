package app.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import app.model.custom.ReviewStatus;
import app.model.custom.UserRole;
import app.model.dto.PostReviewRequest;
import app.model.entity.Reviews;
import app.service.ReviewsService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewsService service;

    @GetMapping("/details/{id}")
    public String getReviewsFromProducts(@PathVariable UUID id, Model model,
            @SessionAttribute(required = false) Claims creds) {

        if (creds == null) {
            List<Reviews> reviews = service.getFromProduct(id, "/products/detail/" + id);
            Double averageReviews = service.averageReviews(reviews);
            Map<Integer, Long> reviewsratingCount = service.countRatings(reviews);

            model.addAttribute("admin", false);
            model.addAttribute("productId", id);
            model.addAttribute("avg", averageReviews);
            model.addAttribute("reviews", reviews);
            model.addAttribute("reviewRatingCount", reviewsratingCount);
            model.addAttribute("eligible", false);

            return "productReviews";

        }

        UserRole currentUserRole = UserRole.valueOf(creds.get("role", String.class));
        UUID userId = UUID.fromString(creds.get("id", String.class));

        String fallback = (currentUserRole == UserRole.ADMIN)
                ? "/admin/products/"
                : "/error/404.html";

        List<Reviews> rawReviews = service.getFromProduct(id, fallback);

        List<Reviews> reviews = rawReviews.stream()
                .filter(review -> review.getStatus() == ReviewStatus.ACCEPTED) // Filter berdasarkan status ACCEPTED
                .collect(Collectors.toList());

        Double averageReviews = service.averageReviews(reviews);
        Map<Integer, Long> reviewsratingCount = service.countRatings(reviews);
        Boolean eligible = service.eligible(rawReviews, userId);
        Boolean isPending = service.isReviewPending(userId, rawReviews);

        model.addAttribute("admin", currentUserRole == UserRole.ADMIN);
        model.addAttribute("productId", id);
        model.addAttribute("avg", averageReviews);
        model.addAttribute("reviews", reviews);
        model.addAttribute("reviewRatingCount", reviewsratingCount);
        model.addAttribute("eligible", eligible);
        model.addAttribute("isPending", isPending);

        System.out.println("\n\n\n\n\n\n" + "eligble : " + eligible + "\n\n\n\n\n\n\n\n\n\n\n\n\n");
        return "productReviews";
    }

    @PostMapping("/post-review/")
    public ResponseEntity<?> postReview(@Valid @ModelAttribute PostReviewRequest request,
            BindingResult result, @SessionAttribute Claims creds) {

        if (creds == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("login dulu sebelum posting review");
        }

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body("Harap isi data dengan benar!");
        }

        UUID userId = UUID.fromString(creds.get("id", String.class));

        return service.saveReviews(request, userId);
    }

}
