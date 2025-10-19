package app.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.apache.commons.imaging.ImageFormat;
import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.ImagingException;
import org.springframework.web.multipart.MultipartFile;

import app.exception.ImageNotValidException;

public class ImageUtils {

    private static final long MAXIMUM_IMAGE_SIZE = 2 * 1024 * 1024; // 2 MB aja yak, bira ga pernu servernya

    private static final String imageUploadDir = "uploads";

    public static ImageFormat isValidImage(MultipartFile imageFile, String fallback) {

        if (imageFile.isEmpty()) {
            throw new ImageNotValidException("file gambar tidak boleh kosong", fallback);
        }

        if (imageFile.getSize() > MAXIMUM_IMAGE_SIZE) {
            throw new ImageNotValidException("file terlalu besar, maksimal 2MB", fallback);
        }

        try {
            byte[] bytes = imageFile.getBytes();
            ImageFormat format = Imaging.guessFormat(bytes);

            if (format == ImageFormats.UNKNOWN) {
                System.out.println("\n\n\n\n\n\n" + "error image kepanggil");
                throw new ImageNotValidException("File bukan gambar yang valid", fallback);
            }
            return format;

        } catch (ImagingException e) {
            System.out.println("\n\n\n\n\n\n" + "error image kepanggil");
            throw new ImageNotValidException("File bukan gambar yang valid", fallback);
        } catch (IOException e) {
            System.out.println("\n\n\n\n\n\n" + "error image kepanggil");
            throw new ImageNotValidException("Gagal membaca file", fallback);
        }

    }

    public static String saveImage(MultipartFile file, String ext) {
        Path uploadPath = Paths.get(imageUploadDir).toAbsolutePath().normalize();

        // Buat folder kalau belum ada
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Generate nama baru pakai UUID
        String fileName = UUID.randomUUID().toString() + "." + ext;

        // Path tujuan akhir
        Path targetLocation = uploadPath.resolve(fileName);

        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileName;

    }

    public static void deleteFile(String fileName) {
        try {
            Path filePath = Paths.get(imageUploadDir, fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Gagal menghapus file", e);
        }
    }

    public static String getUploadDir() {
        return imageUploadDir;
    }

}
