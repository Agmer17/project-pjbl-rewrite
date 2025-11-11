package app.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;

import app.model.custom.ReviewStatus;
import app.model.custom.UserRole;
import app.model.entity.Product;
import app.model.entity.ProductProjection;
import app.model.entity.Reviews;
import app.service.ProductService;
import app.service.ReviewsService;
import io.jsonwebtoken.Claims;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService service;

    @Autowired
    private ReviewsService reviewsService;

    @GetMapping("/detail/{id}")
    public String getProductDetails(@PathVariable UUID id, Model model,
            @SessionAttribute(required = false) Claims creds) {

        if (creds == null) {

            Product productDetails = service.getProductDetails(id, "/error/404.html");
            List<Reviews> rawReviews = reviewsService.getFromProduct(id, "/error/404.html");

            List<Reviews> reviews = rawReviews.stream().filter(r -> r.getStatus() == ReviewStatus.ACCEPTED).toList();
            Double averageReviews = reviews.stream().mapToInt(Reviews::getRating)
                    .average().orElse(0.0);
            Map<Integer, Long> reviewsratingCount = reviewsService.countRatings(reviews);

            model.addAttribute("avg", averageReviews);
            model.addAttribute("reviews", reviews);
            model.addAttribute("reviewRatingCount", reviewsratingCount);
            model.addAttribute("admin", false);
            model.addAttribute("product", productDetails);

            return "AdminProductDetail";

        }

        UserRole currentUserRole = UserRole.valueOf(creds.get("role", String.class));

        Product productDetails = service.getProductDetails(id, "/error/404.html");
        List<Reviews> rawReviews = reviewsService.getFromProduct(id, "/error/404.html");

        List<Reviews> reviews = rawReviews.stream().filter(r -> r.getStatus() == ReviewStatus.ACCEPTED).toList();
        Double averageReviews = reviews.stream().mapToInt(Reviews::getRating)
                .average().orElse(0.0);
        Map<Integer, Long> reviewsratingCount = reviewsService.countRatings(reviews);

        model.addAttribute("avg", averageReviews);
        model.addAttribute("reviews", reviews);
        model.addAttribute("reviewRatingCount", reviewsratingCount);
        model.addAttribute("admin", currentUserRole == UserRole.ADMIN);

        model.addAttribute("product", productDetails);
        return "AdminProductDetail";
    }

    @GetMapping({ "", "/" })
    public String getProductPage(Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(name = "cat", required = false) UUID categoryId,
            @RequestParam(name = "ord", required = false, defaultValue = "desc") String orderBy,
            @RequestParam(name = "q", required = false, defaultValue = "") String search) {
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("order", orderBy);
        model.addAttribute("search", search);
        model.addAttribute("categories", service.getAllCategory());
        Page<ProductProjection> products = service.getAllProducts(page, categoryId, orderBy, search);

        model.addAttribute("products", products.getContent());
        return "Products";

    }

    @GetMapping("/search")
    public String getSearchPage() {
        return "Search";
    }

    @GetMapping("/prew/get-all")
    @ResponseBody
    public List<ProductProjection> getPreviewData() {
        return service.getAllProductsPreview();
    }
    

}
