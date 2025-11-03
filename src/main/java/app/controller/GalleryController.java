package app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import app.service.ProductImageService;

@Controller
public class GalleryController {
    
    @Autowired
    private ProductImageService imageService;

}
