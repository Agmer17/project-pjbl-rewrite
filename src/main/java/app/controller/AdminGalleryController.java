package app.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import app.model.entity.ProductImage;
import app.service.ProductImageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@Controller
@RequestMapping("/admin/gallery")
public class AdminGalleryController {

    @Autowired
    private ProductImageService service;


    @GetMapping({"/", ""})
    public String getGalleryImage(Model model) {
        List<ProductImage> images = service.getAllImage();
        model.addAttribute("images", images);
        
        return "admin/AdminGallery";
    }

    @GetMapping("/get-non-gallery")
    @ResponseBody
    public List<ProductImage> getNonGalleryImage() {
        return service.getNonGallery();
    }


    @PostMapping("/update-gallery")
    @ResponseBody
    public void addNewGalleryImage(@RequestBody List<UUID> entityId) {
        service.setNewGalleryFromImages(entityId);
    }


    @PostMapping("/delete-gallery")
    @ResponseBody
    public void removeFromGallery(@RequestBody List<UUID> entity) {
        service.removeFromGallery(entity);
    }
    
    
    
    


    
}
