package app.service;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.imaging.ImageFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import app.exception.ImageNotValidException;
import app.model.entity.Product;
import app.model.entity.ProductImage;
import app.repository.ProductImageRepository;
import app.utils.ImageUtils;
import jakarta.transaction.Transactional;

@Service
public class ProductImageService {

        @Autowired
        private ImageUtils imageUtils;

        @Autowired
        private ProductImageRepository repo;


        @Transactional
        public void saveAllImages(List<MultipartFile> allImages, String fallback, Product product) {

                List<ImageFormat> formats = allImages.stream()
                                .map(f -> imageUtils.isValidImage(f, fallback))
                                .toList();

                if (formats.isEmpty()) {
                        throw new ImageNotValidException("Tidak ada gambar valid", fallback);
                }

                List<String> exts = formats.stream()
                                .map(f -> f.getDefaultExtension().replace(".", ""))
                                .toList();

                List<String> savedFileNames = imageUtils.saveImageBatch(allImages, exts);

                List<ProductImage> productImages = new ArrayList<>();

                // List<ProductImage> productImages = savedFileNames.stream()
                //                 .map(fileName -> ProductImage.builder()
                //                                 .imageFileName(fileName)
                //                                 .product(product)
                //                                 .galleryImage(false)
                //                                 .build())
                //                 .toList();

                for (int i = 0; i < savedFileNames.size(); i++) {
                        productImages.add(
                                ProductImage.builder()
                                .imageFileName(savedFileNames.get(i))
                                .product(product)
                                .imageOrder(i+1)
                                .build()
                        );
                }

                repo.saveAll(productImages);

        }

        public void deletProductImage(Product product) {

                List<ProductImage> images = repo.findAllByProduct(product);

                List<String> imagesFileNames = images.stream()
                .map(ProductImage::getImageFileName)
                .toList();

                imageUtils.deleteFilesBatch(imagesFileNames);
                
        }

}
