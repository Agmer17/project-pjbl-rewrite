package app.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SignUpRequest {
    
    @NotBlank(message = "username tidak boleh kosong")
    @Size(min = 4, max = 255, message = "nama minimal 4 dan maksimal 255 karakter")
    private String username;

    @NotBlank(message = "email tidak boleh kosong")
    @Size(min = 4, max = 255, message = "email minimal 4 karakter dan maksimal 255 karakter")
    @Email(message = "email harus valid")
    private String email;

    @JsonProperty("full_name")
    @NotBlank(message = "nama tidak boleh kosong")
    private String fullName;

    @NotBlank(message = "Password tidak boleh kosong")
    @Size(max = 255, min = 4, message = "password minimal 4 karakter dan maksimal 255 karakter")
    private String password;

}
