package app.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import app.exception.DataNotFoundEx;
import app.exception.ImageNotValidException;
import app.model.dto.ProductPostDto;
import app.model.dto.UpdateProductRequest;
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

    public Product getProductDetails(UUID id) {

        return productRepository.findDetailById(id)
                .orElseThrow(() -> new DataNotFoundEx("barang mungkin telah terhapus", "/admin/products/"));
    }

    @Transactional
    public void editProduct(UpdateProductRequest request) {
        Product product = productRepository.findById(request.getId())
        .orElseThrow(()-> new DataNotFoundEx("Produk mungkin telah terhapus! hubungi admin lainnya", "/admin/products/"));
        // System.out.printf("\n\n\n\n\n\n\n\n\n\n size array file baru : %d\n\n\n\n\n\n\n\n\n\n\n", request.getNewImagesFiles().size());
        // System.out.printf("\n\n\n\n\n\n\n\n\n\n size array file baru : %s\n\n\n\n\n\n\n\n\n\n\n", request.getNewImagesFiles().get(0).getOriginalFilename());


        if (request.getImageToDelete() != null &&!request.getImageToDelete().isEmpty()) {

            productImageService.deleteImageById(request.getImageToDelete(), product);
        }

        if (request.getUpdatedImageIds() != null && request.getUpdatedImageFiles() != null) {
            
            if (!validateUpdatedImages(request)) {
                throw new ImageNotValidException("/admin/products/edit/"+request.getId(), "Data gambar tidak valid");
            }

            productImageService.editImageFromRequest(request.getUpdatedImageIds(), request.getUpdatedImageFiles(), product);
        }

        
        if (request.getNewImagesFiles() != null) {
            
            if (!request.getNewImagesFiles().isEmpty()) {
                productImageService.saveAllImageToExistingProduct(request.getNewImagesFiles(), "/admin/products/edit/"+product.getId(), product);
            }
        }


        product.setName(request.getName());
        product.setDesc(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(categoryService.getById(request.getCategoryId()));



    }

    private Boolean validateUpdatedImages(UpdateProductRequest request) {
        List<UUID> ids = request.getUpdatedImageIds();
        List<MultipartFile> files = request.getUpdatedImageFiles();

        // Both null or both empty = valid
        boolean idsEmpty = ids == null || ids.isEmpty();
        boolean filesEmpty = files == null || files.isEmpty();

        if (idsEmpty && filesEmpty)
            return null;
        if (idsEmpty != filesEmpty)
            return false;
        if (ids.size() != files.size()) {
            return false;
        }

        // Cek validity dalam 1 loop
        for (int i = 0; i < ids.size(); i++) {
            if (ids.get(i) == null)
                return false;
            if (files.get(i) == null || files.get(i).isEmpty()) {
                return false;
            }
        }

        return true;
    }

}
