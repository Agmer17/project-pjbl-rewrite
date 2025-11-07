package app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import app.model.entity.ProductImage;
import app.service.ProductImageService;

@Controller
@RequestMapping("/gallery")
public class GalleryController {
    
    @Autowired
    private ProductImageService imageService;


    @GetMapping("/")
    public String getPage(Model model) {
        List<ProductImage> images = imageService.getGalleryAllImage();

        model.addAttribute("images", images);
        return "userGallery";

    }

}
