package app.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import app.model.entity.Reviews;
import app.service.ReviewsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;




@Controller
@RequestMapping("/admin/reviews")
public class AdminReviewsController {

    @Autowired
    private ReviewsService service;


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteReviews(@PathVariable UUID id) {

        Reviews review = service.getById(id);

        if (review == null) {
            return ResponseEntity.notFound().build();
        }

        return service.deleteReviews(id);

    } 

    @PostMapping("/acc/{id}")
    public ResponseEntity<?> updateToAccepted(@RequestBody UUID id) {
         return service.acceptReview(id);
    }
    

    @GetMapping("")
    public String getPage(Model model) {

        model.addAttribute("pendingReviews", service.getPendingReviews());
        model.addAttribute("approvedReviews", service.getAllReviews());
        return "admin/AdminReviews";
    }
}
