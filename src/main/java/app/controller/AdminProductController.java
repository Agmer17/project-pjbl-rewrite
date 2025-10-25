package app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import app.exception.FieldValidationException;
import app.model.dto.ProductPostDto;
import app.model.entity.ProductCategory;
import app.model.entity.ProductProjection;
import app.service.ProductService;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;



@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    @GetMapping({"/", ""})
    public String getProductDashboard(Model model, @RequestParam(defaultValue = "0") int page) {
        Page<ProductProjection> pageable = productService.getAllProducts(page);

        model.addAttribute("products", pageable.getContent());
        return "adminProductDashboard";
    }

    @GetMapping("/add")
    public String getAddProductPage(Model model) {

        List<ProductCategory> categories = productService.getAllCategory();
        model.addAttribute("categories", categories);
        return "adminAddProduct";
    }

    @PostMapping("/add")
    public String postProductData(@Valid @ModelAttribute ProductPostDto productPost, 
    BindingResult bindingResult,
    RedirectAttributes model) {
        
        if (bindingResult.hasErrors()) {
            throw new FieldValidationException("Harap isi data dengan benar", bindingResult, "/admin/products/add");
        }
        
        productService.saveProduct(productPost);


        model.addFlashAttribute("successMsg", "berhasil menambah produk");
        return "redirect:/admin/products/add";
    }

    
    
    
}
