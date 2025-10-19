package app.service;

import java.util.List;
import java.util.UUID;

import org.apache.commons.imaging.ImageFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.exception.AuthValidationException;
import app.model.dto.UpdateProfileRequest;
import app.model.entity.Users;
import app.model.projection.UserProfileProjection;
import app.repository.UserRepository;
import app.utils.ImageUtils;
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

}
