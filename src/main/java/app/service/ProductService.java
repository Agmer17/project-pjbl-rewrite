package app.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import app.exception.ImageNotValidException;
import app.model.dto.ProductPostDto;
import app.model.entity.Product;
import app.model.entity.ProductCategory;
import app.model.entity.ProductProjection;
import app.repository.ProductRepository;

@Service
public class ProductService {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductImageService productImageService;

    @Autowired
    private ProductRepository productRepository;

    public List<ProductCategory> getAllCategory() {
        return categoryService.getAllCategory();
    }

    public void saveProduct(ProductPostDto data) {

        if (data.getAllProductImage().isEmpty()) {
            throw new ImageNotValidException("gambar tidak boleh kosong",
                    "/admin/category/add");
        }

        Product product = Product
                .builder()
                .name(data.getName())
                .desc(data.getDesc())
                .price(data.getPrice())
                .category(categoryService.getById(data.getCategoryId()))
                .build();

                
        productRepository.save(product);


        productImageService.saveAllImages(data.getAllProductImage(), "/admin/products/add", product);


    }

    public Page<ProductProjection> getAllProducts(int page) {

        Pageable pageable = PageRequest.of(page, 10);

        return productRepository.findAllProductsForDashboard(pageable);
    }

}
