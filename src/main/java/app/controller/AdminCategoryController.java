package app.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import app.model.dto.ProductCategoryPostDto;
import app.model.entity.ProductCategory;
import app.service.CategoryService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/category")
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping("/add")
    public ResponseEntity<?> addCategory(@Valid @RequestBody ProductCategoryPostDto request,
            BindingResult result) {

        if (result.hasErrors()) {

            return ResponseEntity.badRequest().body(request);

        }
        return categoryService.saveCategory(request);

    }

    @PostMapping("/edit/{id}")
    public ResponseEntity<ProductCategory> editCategory(
            @PathVariable UUID id,
            @Valid @ModelAttribute ProductCategoryPostDto dto,
            BindingResult result) {

        if (result.hasErrors()) {
            
        }
         

        return categoryService.editCategory(dto, id);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable UUID id) {
        return categoryService.deletecategory(id);
    }

}
