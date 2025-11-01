package app.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import app.exception.FieldValidationException;
import app.model.dto.ProductPostDto;
import app.model.dto.UpdateProductRequest;
import app.model.entity.Product;
import app.model.entity.ProductCategory;
import app.model.entity.ProductProjection;
import app.model.projection.DashboardStatsProjection;
import app.service.ProductService;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;


@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    @GetMapping({ "/", "" })
    public String getProductDashboard(Model model, 
    @RequestParam(defaultValue = "0") int page, 
    @RequestParam(name = "cat", required = false) UUID categoryId,
    @RequestParam(name = "ord", required = false, defaultValue = "desc") String orderBy) {
        Page<ProductProjection> pageable = productService.getAllProducts(page, categoryId, orderBy);
        DashboardStatsProjection stats = productService.getProductStatsData();

        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("totalProducts", stats.getTotalProducts());
        model.addAttribute("totalCategories", stats.getTotalCategories());
        model.addAttribute("totalImages", stats.getTotalImages());
        model.addAttribute("products", pageable.getContent());
        model.addAttribute("currentPage", pageable.getNumber());
        model.addAttribute("totalPages", pageable.getTotalPages());
        model.addAttribute("hasNext", pageable.hasNext());
        model.addAttribute("hasPrevious", pageable.hasPrevious());
        model.addAttribute("categories", productService.getAllCategory());
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
            System.out.println(bindingResult.getFieldErrors());
            throw new FieldValidationException("Harap isi data dengan benar", bindingResult, "/admin/products/add");
        }

        productService.saveProduct(productPost);

        model.addFlashAttribute("successMsg", "berhasil menambah produk");
        return "redirect:/admin/products/add";
    }

    @GetMapping("/edit/{id}")
    public String getEditPage(@PathVariable UUID id, Model model) {

        Product product = productService.getProductDetails(id);

        model.addAttribute("product", product);
        model.addAttribute("formRequest", new UpdateProductRequest());
        model.addAttribute("categories", productService.getAllCategory());
    
        return "admin/EditProduct";
    }
    

    @PostMapping("/edit/{id}")
    public String postEditProduct(@PathVariable UUID id, 
    @Valid @ModelAttribute UpdateProductRequest req,
    BindingResult bindingResult,
    RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            throw new FieldValidationException("Harap isi data dengan benar", bindingResult, "/admin/products/edit/"+id); 

        }

        productService.editProduct(req);


        redirectAttributes.addFlashAttribute("successMsg", "Berhasil mengupdate data");
        return "redirect:/admin/products/edit/"+id;
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);

        return "redirect:/admin/products/";
    }

    @GetMapping("/detail/{id}")
    public String getMethodName(@PathVariable UUID id, Model model) {

        Product productDetails = productService.getProductDetails(id);

        model.addAttribute("product", productDetails);

        return "AdminProductDetail";
        
    }
    

}
