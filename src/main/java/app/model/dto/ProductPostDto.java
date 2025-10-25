package app.model.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductPostDto {
    
    @NotBlank(message = "nama tidak boleh kosong")
    @Size(min = 4, max = 255, message = "nama produk 4 - 255 karakter")
    private String name;
    
    @NotBlank(message = "deskripsi layanan tidak boleh kosong")
    private String desc;

    @NotNull(message = "harga tidak boleh kosong")
    private BigDecimal price;

    @NotNull(message = "katergori tidak boleh kosong!")
    private UUID categoryId;

    private List<MultipartFile> allProductImage;

}
