package app.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import app.model.projection.UserProfileProjection;
import app.service.ProductImageService;
import app.service.ProductService;
import app.service.ReviewsService;
import app.service.UserService;
import io.jsonwebtoken.Claims;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private UserService userService;

    @Autowired 
    private  ProductService productService;

    @Autowired 
    private ProductImageService productImageService;

    @Autowired 
    private ReviewsService reviewsService;

    @GetMapping("/")
    public String getAdminDashboard(@SessionAttribute Claims creds, Model model) {

        UUID adminId = UUID.fromString(creds.get("id", String.class));

        UserProfileProjection currentAdminData = userService.getUserProfileById(adminId, "/login");

        model.addAttribute("currentAdmin", currentAdminData);
        model.addAttribute("totalUsers", userService.countUser());
        model.addAttribute("totalProducts", productService.countProduct());
        model.addAttribute("totalReviews", reviewsService.countReview());
        model.addAttribute("totalGalleryImages", productImageService.countProductImages());
        model.addAttribute("recentProducts", productService.getLatestProduct());
        model.addAttribute("recentReviews", reviewsService.getLatesReviewData());


        return "adminDashboard";

    }


}
