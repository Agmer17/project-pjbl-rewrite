package app.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

                List<ProductImage> productImages = new ArrayList<>();

                List<String> savedFileNames = saveImageToServer(allImages, fallback);

                for (int i = 0; i < savedFileNames.size(); i++) {
                        productImages.add(
                                        ProductImage.builder()
                                                        .imageFileName(savedFileNames.get(i))
                                                        .product(product)
                                                        .galleryImage(true)
                                                        .imageOrder(i + 1)
                                                        .build());
                }

                repo.saveAll(productImages);

        }

        public void saveAllImageToExistingProduct(List<MultipartFile> allImages, String fallback, Product product) {

                List<String> newFileNames = saveImageToServer(allImages, fallback);

                List<ProductImage> currentImages = product.getImages();

                int lastOrder = currentImages.isEmpty()
                                ? 0
                                : currentImages.get(currentImages.size() - 1).getImageOrder();

                for (int i = 0; i < newFileNames.size(); i++) {
                        int order = lastOrder + i + 1;

                        ProductImage newImage = ProductImage.builder()
                                        .imageFileName(newFileNames.get(i))
                                        .product(product)
                                        .galleryImage(true)
                                        .imageOrder(order + 1)
                                        .build();

                        currentImages.add(newImage); // otomatis di track ygy
                }
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

                List<ProductImage> deletedImages = product.getImages().stream()
                                .filter(img -> imageIds.contains(img.getId()))
                                .toList();

                List<String> imageFileNames = deletedImages.stream().map(ProductImage::getImageFileName).toList();

                List<Integer> deletedOrders = deletedImages.stream()
                                .map(ProductImage::getImageOrder)
                                .sorted()
                                .toList();

                shiftImageOrder(product.getImages(), deletedOrders);
                product.getImages().removeIf(img -> imageIds.contains(img.getId()));

                deleteAllFiles(imageFileNames);
        }

        private void deleteAllFiles(List<String> imageFiles) {
                imageUtils.deleteFilesBatch(imageFiles);
        }

        @Transactional
        public List<ProductImage> editImageFromRequest(
                        List<UUID> updatedImageId,
                        List<MultipartFile> updatedImageFile,
                        Product product) {

                List<ProductImage> currentImages = product.getImages();

                Map<UUID, MultipartFile> imageMap = IntStream.range(0, updatedImageId.size())
                                .boxed()
                                .collect(Collectors.toMap(updatedImageId::get, updatedImageFile::get));

                currentImages.stream()
                                .filter(img -> imageMap.containsKey(img.getId()))
                                .forEach(img -> {
                                        MultipartFile newFile = imageMap.get(img.getId());

                                        imageUtils.deleteFile(img.getImageFileName());

                                        ImageFormat newFileExt = imageUtils.isValidImage(newFile,
                                                        "/admins/products/edit/" + product.getId());

                                        String newFileName = imageUtils.saveImageAsync(newFile,
                                                        newFileExt.getDefaultExtension().replace(".", "")).join();

                                        // Update field di entity yang sudah managed
                                        img.setImageFileName(newFileName);
                                });

                // Jangan hapus/hapus list — biarkan Hibernate track perubahan di object
                // existing
                // Karena object di currentImages masih managed, semua perubahan otomatis
                // di-flush

                return currentImages;
        }

        // THIS SHIT IS SLOW ASF
        // UNTUNGNYA MAKS GAMBAR JUGA CMN 5
        // BUT MUNGKIN BAKAL KERASA KALO UDAH 20++ GAMBAR
        // BUT WHO CARES!
        // make it work, make it right, make it fase - kent beck
        private void shiftImageOrder(List<ProductImage> remainingImages, List<Integer> deletedOrders) {

                // for future programmer, tolong optimisasi ini
                // soalnya buat delete gambar sampe 3 query lebih, nanti ubah aja jpa nya biar
                // jadi 2 query

                remainingImages.sort(Comparator.comparing(ProductImage::getImageOrder));

                for (Integer deletedOrder : deletedOrders) {
                        remainingImages.stream()
                                        .filter(img -> img.getImageOrder() > deletedOrder)
                                        .forEach(img -> img.setImageOrder(img.getImageOrder() - 1));
                }

                repo.saveAll(remainingImages);
        }

        private List<String> saveImageToServer(List<MultipartFile> allImages, String fallback) {
                List<ImageFormat> formats = allImages.stream()
                                .map(r -> imageUtils.isValidImage(r, fallback))
                                .toList();

                if (formats.isEmpty()) {
                        throw new ImageNotValidException("Tidak ada gambar valid", fallback);
                }

                List<String> exts = formats.stream()
                                .map(f -> f.getDefaultExtension().replace(".", ""))
                                .toList();

                List<String> savedFileNames = imageUtils.saveImageBatch(allImages, exts);

                return savedFileNames;
        }

        public List<ProductImage> getAllImage() {
                return repo.findAll();
        }

        public List<ProductImage> getNonGallery() {
                return repo.findAllByGalleryImageFalse();
        }

        @Transactional
        public void setNewGalleryFromImages(List<UUID> ids) {
                List<ProductImage> images = repo.findAllById(ids);
                images.forEach(img -> img.setGalleryImage(true));
                // repo.saveAll(images); // optional, bisa dipakai untuk memastikan
        }

        @Transactional
        public void removeFromGallery(List<UUID> ids) {
                List<ProductImage> images = repo.findAllByIdIn(ids);

                images.forEach(img -> img.setGalleryImage(false));
        }

}
