package app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import app.model.dto.ProductCategoryPostDto;
import app.service.CategoryService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/category")
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping("/add")
    public ResponseEntity<?> addCategory(@Valid @RequestBody ProductCategoryPostDto request) {
        categoryService.saveCategory(request);
        return ResponseEntity.ok().build();

    }


    
}
