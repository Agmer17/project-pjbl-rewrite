package app.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.imaging.ImageFormat;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import app.exception.AuthValidationException;
import app.model.custom.Gender;
import app.model.dto.AdminAddUserDto;
import app.model.dto.UpdateProfileRequest;
import app.model.entity.Users;
import app.model.projection.UserProfileProjection;
import app.repository.UserRepository;
import app.utils.ImageUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepo;

    public UserProfileProjection getUserProfileById(UUID id) {
        return userRepo.findProfileById(id).get();
    }

    @Transactional
    public void updateUserProfile(UUID userId, UpdateProfileRequest updateProfileRequest) {
        Users user = userRepo.findById(userId)
                .orElseThrow(() -> new AuthValidationException("User tidak ditemukan", "/home"));

        boolean usernameChanged = !user.getUsername().equals(updateProfileRequest.getUsername());
        boolean emailChanged = !user.getEmail().equals(updateProfileRequest.getEmail());

        if (usernameChanged || emailChanged) {
            List<Users> existingUsers = userRepo.findAllByUsernameOrEmail(
                    updateProfileRequest.getUsername(),
                    updateProfileRequest.getEmail());

            boolean hasDuplicate = existingUsers.stream()
                    .anyMatch(existingUser -> !existingUser.getId().equals(userId));

            if (hasDuplicate) {
                if (usernameChanged && existingUsers.stream()
                        .anyMatch(u -> !u.getId().equals(userId) &&
                                u.getUsername().equals(updateProfileRequest.getUsername()))) {
                    throw new AuthValidationException("Username sudah terdaftar", "/user/my-profile");
                }
                if (emailChanged && existingUsers.stream()
                        .anyMatch(u -> !u.getId().equals(userId) &&
                                u.getEmail().equals(updateProfileRequest.getEmail()))) {
                    throw new AuthValidationException("Email sudah terdaftar", "/user/my-profile");
                }
            }
        }

        user.setUsername(updateProfileRequest.getUsername());
        user.setEmail(updateProfileRequest.getEmail());
        user.setFullName(updateProfileRequest.getFullName());
        user.setPhoneNumber(updateProfileRequest.getPhoneNumber());
        user.setGender(updateProfileRequest.getGender());

        if (updateProfileRequest.getProfilePicture() != null &&
                !updateProfileRequest.getProfilePicture().isEmpty()) {

                    System.out.println("\n\n\n\n\n ini save gambar \n\n\n\n\n");
            

            ImageFormat imageFormat = ImageUtils.isValidImage(updateProfileRequest.getProfilePicture(),
                    "/user/my-profile");
            
            String fileExt = imageFormat.getDefaultExtension();

            String fileNameUrl = ImageUtils.saveImage(updateProfileRequest.getProfilePicture(), fileExt);

            if (user.getProfilePicture() != null) {
                ImageUtils.deleteFile(user.getProfilePicture());
            }

            user.setProfilePicture(fileNameUrl);

           System.out.println("\n\n\n\n\n gambarnya udah di save ke db \n\n\n\n\n");
            

        }

        userRepo.save(user);
    }

    public void saveNewUser(AdminAddUserDto request) {

        Optional<Users> existingUser = userRepo.findByUsernameOrEmail(request.getUsername(), request.getEmail());

        existingUser.ifPresent(_ -> {
            if (existingUser.get().getUsername().equals(request.getUsername())) {
                throw new AuthValidationException("Username sudah digunakan", "/admin/users/add", request);
            }
            if (existingUser.get().getEmail().equals(request.getEmail())) {
                throw new AuthValidationException("Email sudah digunakan", "/admin/users/add", request);
            }
        });

         Users newUser = Users.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .role(request.getUserRole())
                .gender(Gender.LAINNYA)
                .password(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt(12)))
                .build();

        userRepo.save(newUser);

    }

    public Page<UserProfileProjection> findAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return userRepo.findAllProjectedBy(pageable);


    }

    public void deleteUsers(UUID id) {
        if (!userRepo.existsById(id)) {
            throw new EntityNotFoundException("User dengan ID " + id + " tidak ditemukan");
        } 

        userRepo.deleteById(id);
    }

}
