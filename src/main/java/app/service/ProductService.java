package app.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import app.exception.DataNotFoundEx;
import app.exception.ImageNotValidException;
import app.model.dto.ProductPostDto;
import app.model.entity.Product;
import app.model.entity.ProductCategory;
import app.model.entity.ProductProjection;
import app.model.projection.DashboardStatsProjection;
import app.repository.ProductRepository;
import jakarta.transaction.Transactional;

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

    @Transactional
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

    public Page<ProductProjection> getAllProducts(int page, UUID categoryId, String orderBy) {

        Pageable pageable = PageRequest.of(page, 10,
                Sort.by(orderBy.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                        "createdAt"));

        if (categoryId != null) {
            return productRepository.findAllProductsForDashboard(categoryId, pageable);

        }
        return productRepository.findAllProductsForDashboard(pageable);
    }

    public DashboardStatsProjection getProductStatsData() {
        return productRepository.getDashboardStats();
    }

    public List<ProductCategory> getAllCategories() {
        return categoryService.getAllCategory();
    }

    public void deleteProduct(UUID id) {

        Product product = productRepository.findById(id).orElseThrow(() -> new DataNotFoundEx(
                "produk tidak ditemukan, mungkin sudah terhapus",
                "/admin/products/"));

        productImageService.deletProductImage(product);

        productRepository.delete(product);


    }

}
