package app.model.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateProductRequest {

    @NotNull
    private UUID id;

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotNull
    private BigDecimal price;

    @NotNull
    private UUID categoryId;

    // Tambah gambar baru
    private List<MultipartFile> newImagesFiles;

    // Gambar yang dihapus
    private List<UUID> imageToDelete;

    // Gambar yang diupdate (id + file baru)
    private List<UUID> updatedImageIds;
    private List<MultipartFile> updatedImageFiles;
}
