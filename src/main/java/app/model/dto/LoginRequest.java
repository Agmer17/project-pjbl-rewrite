package app.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message = "username tidak boleh kosong")
    @Size(min = 4, max = 255, message = "nama minimal 4 dan maksimal 255 karakter")
    private String username;

    @NotBlank(message = "Password tidak boleh kosong")
    @Size(min = 4, max = 255, message = "password minimal 4 karakter dan maksimal 255 karakter")
    private String password;
    
}
