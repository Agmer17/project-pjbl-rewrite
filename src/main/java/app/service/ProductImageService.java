package app.service;

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

@Service
public class ProductImageService {

        @Autowired
        private ImageUtils imageUtils;

        @Autowired
        private ProductImageRepository repo;

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

                System.out.println("\n\n\n\n\n\n\n"+savedFileNames+"\n\n\n\n\n\n");

                List<ProductImage> productImages = savedFileNames.stream()
                                .map(fileName -> ProductImage.builder()
                                                .imageFileName(fileName)
                                                .productId(product)
                                                .galleryImage(false)
                                                .build())
                                .toList();

                repo.saveAll(productImages);

        }

}
