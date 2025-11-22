package app.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import app.model.custom.UserRole;
import app.model.dto.LoginRequest;
import app.model.dto.SignUpRequest;
import app.service.ProductImageService;
import app.service.ProductService;
import app.service.ReviewsService;
import io.jsonwebtoken.Claims;
import org.springframework.web.bind.annotation.RequestParam;




@Controller
public class PageController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductImageService imageService;

    @Autowired
    private ReviewsService reviewsService;

    @GetMapping("/")
    public String homePageRedirect() {

        return "redirect:/home";
    }
    

    @GetMapping("/home")
    public String homePage(@SessionAttribute(required = false) Claims creds,Model model) {

        UserRole role = UserRole.CUSTOMER;

        if (creds != null) {
            role = UserRole.valueOf(creds.get("role", String.class));
        }
        
        model.addAttribute("products",productService.getRandomProduct(6));
        model.addAttribute("images", imageService.getRandom(6));
        model.addAttribute("admin", role == UserRole.ADMIN);
        model.addAttribute("randomReview", reviewsService.getRandom(3));

        return "home";
    }
    

    @GetMapping("/login")
    public String loginPage(Model model) {

        if (!model.containsAttribute("formRequest")) {
        model.addAttribute("formRequest", new LoginRequest());
    }
        
        return "login";
    }

    @GetMapping("/sign-up")
    public String signUpPage(Model model) {
        if (!model.containsAttribute("formRequest")) {
            model.addAttribute("formRequest", new SignUpRequest());
        }
        return "signup";
    }

    @GetMapping("/faq")
    public String getFaqPage() {
        return "Faq";
    }
    
    
    
}
