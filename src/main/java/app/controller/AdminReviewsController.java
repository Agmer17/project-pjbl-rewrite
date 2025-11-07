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
    private ReviewsService servive;


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteReviews(@PathVariable UUID id) {

        Reviews review = servive.getById(id);

        if (review == null) {
            return ResponseEntity.notFound().build();
        }

        return servive.deleteReviews(id);

    } 

    @PostMapping("/acc/{id}")
    public ResponseEntity<?> updateToAccepted(@RequestBody UUID id) {
         return servive.acceptReview(id);
    }
    

    @GetMapping("")
    public String getPage(Model model) {


        model.addAttribute("pendingReviews", servive.getPendingReviews());
        return "admin/AdminReviews";
    }
}
