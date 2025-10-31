package app.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

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

                for (int i = 0; i < savedFileNames.size(); i++) {
                        productImages.add(
                                        ProductImage.builder()
                                                        .imageFileName(savedFileNames.get(i))
                                                        .product(product)
                                                        .imageOrder(i + 1)
                                                        .build());
                }

                repo.saveAll(productImages);

        }

        public void deletProductImage(Product product) {

                List<ProductImage> images = repo.findAllByProduct(product);

                List<String> imagesFileNames = images.stream()
                                .map(ProductImage::getImageFileName)
                                .toList();

                deleteAllFiles(imagesFileNames);

        }

        @Transactional
        public void deleteImageById(List<UUID> imageIds, Product product) {

                List<ProductImage> images = repo.findAllByIdIn(imageIds);

                // geser indexnya

                List<String> imageFileNames = images.stream().map(ProductImage::getImageFileName).toList();

                List<Integer> deletedOrders = images.stream()
                                .map(ProductImage::getImageOrder)
                                .sorted()
                                .toList();

                shiftImageOrder(product, deletedOrders);
                
                repo.deleteAllByIdIn(imageIds);

                deleteAllFiles(imageFileNames);
        }

        private void deleteAllFiles(List<String> imageFiles) {
                imageUtils.deleteFilesBatch(imageFiles);
        }

        // THIS SHIT IS SLOW ASF
        // UNTUNGNYA MAKS GAMBAR JUGA CMN 5
        // BUT MUNGKIN BAKAL KERASA KALO UDAH 20++ GAMBAR
        // BUT WHO CARES!
        // make it work, make it right, make it fase - kent beck
        private void shiftImageOrder(Product product, List<Integer> deletedOrders) {

                // for future programmer, tolong optimisasi ini
                // soalnya buat delete gambar sampe 3 query lebih, nanti ubah aja jpa nya biar
                // jadi 2 query
                List<ProductImage> remainingImages = repo.findAllByProduct(product);

                remainingImages.sort(Comparator.comparing(ProductImage::getImageOrder));

                int currentOrder = 1;
                for (ProductImage img : remainingImages) {
                        img.setImageOrder(currentOrder++);
                }

                repo.saveAll(remainingImages);
        }

}
