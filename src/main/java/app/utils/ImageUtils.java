package app.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.commons.imaging.ImageFormat;
import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.Imaging;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import app.exception.ImageNotValidException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Component
@Getter
public class ImageUtils {

    private static final long MAXIMUM_IMAGE_SIZE = 2 * 1024 * 1024; // 2 MB
    private static final String IMAGE_UPLOAD_DIR = "uploads";

    public ImageFormat isValidImage(MultipartFile imageFile, String fallback) {
        if (imageFile.isEmpty()) {
            throw new ImageNotValidException(fallback, "gambar tidak boleh kosong");
        }

        if (imageFile.getSize() > MAXIMUM_IMAGE_SIZE) {
            throw new ImageNotValidException(fallback, "Maksimal besar gambar adalah 2MB");
        }

        try {
            byte[] bytes = imageFile.getBytes();
            ImageFormat format = Imaging.guessFormat(bytes);
            if (format == ImageFormats.UNKNOWN) {
                throw new ImageNotValidException("File bukan gambar yang valid", fallback);
            }
            return format;
        } catch (IOException e) {
            throw new ImageNotValidException("Gagal membaca file", fallback);
        }
    }

    public String saveImage(MultipartFile file, String ext) {
        Path uploadPath = Paths.get(IMAGE_UPLOAD_DIR).toAbsolutePath().normalize();

        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = UUID.randomUUID() + "." + ext;
            Path targetLocation = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Gagal menyimpan file: " + e.getMessage(), e);
        }
    }

    @Async
    public CompletableFuture<String> saveImageAsync(MultipartFile file, String ext) {
        return CompletableFuture.completedFuture(saveImage(file, ext));
    }

    public List<String> saveImageBatch(List<MultipartFile> files, List<String> allFileExt) {
        if (files.size() != allFileExt.size()) {
            throw new IllegalArgumentException("Jumlah file dan ekstensi tidak sama!");
        }

        List<CompletableFuture<String>> futures = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            futures.add(saveImageAsync(files.get(i), allFileExt.get(i)));
        }

        // Tunggu semua selesai, meski salah satu error
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // Ambil hasil file name yang sukses
        return futures.stream()
                .map(f -> {
                    try {
                        return f.join();
                    } catch (Exception e) {
                        return null; // abaikan yang gagal
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Async
    public void deleteFile(String fileName) {
        try {
            Path filePath = Paths.get(IMAGE_UPLOAD_DIR, fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Gagal menghapus file", e);
        }
    }

    public String getUploadDir() {
        return IMAGE_UPLOAD_DIR;
    }

    @Async("taskExecutor")
    public void deleteFilesBatch(List<String> fileNames) {
        if (fileNames == null || fileNames.isEmpty()) {
            return;
        }

        fileNames.stream()
                .filter(fileName -> fileName != null && !fileName.isBlank())
                .forEach(fileName -> {
                    try {
                        Path filePath = Paths.get(IMAGE_UPLOAD_DIR, fileName);
                        Files.deleteIfExists(filePath);
                        log.debug("File dihapus: {}", fileName);
                    } catch (IOException e) {
                        log.error("Gagal menghapus file: {}", fileName, e);
                    }
                });
    }
}
