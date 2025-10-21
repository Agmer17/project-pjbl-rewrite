package app.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdatePasswordRequest {

    @NotBlank(message = "password tidak boleh kosong")
    @Size(min = 8, max = 255, message = "password minimal 8 dan maksimal 255 karakter")
    private String newPassword;
    private String token;
    
}
