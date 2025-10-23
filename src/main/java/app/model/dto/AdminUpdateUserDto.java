package app.model.dto;


import org.springframework.web.multipart.MultipartFile;

import app.model.custom.Gender;
import app.model.custom.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminUpdateUserDto {

    @NotBlank(message = "usrname tidak boleh kosong")
    @Size(min = 4, max = 255, message = "username minimal 4 karakter dan maksimal 255")
    private String username;

    @Email(message = "Format email tidak valid")
    @NotBlank(message = "email tidak boleh kosong")
    private String email;

    @NotBlank(message = "Nama lengkap tidak boleh kosong")
    @Size(min = 4, max = 255, message = "Nama lengkap minimal 4 dan maksimal 355 karakter")
    private String fullName;

    private MultipartFile profilePicture;

    private String phoneNumber;

    private Gender gender;

    private String password;

    @NotNull
    private UserRole role;

}
