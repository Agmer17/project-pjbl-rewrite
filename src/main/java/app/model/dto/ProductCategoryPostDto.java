package app.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductCategoryPostDto {

    @NotBlank(message = "nama kategori tidak boleh kosong")
    @Size(min = 2, max = 255, message = "kategori minimal 2 dan maksimal 255 karakter")
    private String name;

    private String desc;


    
}
