package app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import app.model.entity.ProductCategory;
import app.service.CategoryService;

@Controller
@RequestMapping("/category")
public class CategoryController {
    
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/get-all")
    public ResponseEntity<List<ProductCategory>> getAllCategory() {

        return ResponseEntity.ok().body(categoryService.getAllCategory());

    }
}
