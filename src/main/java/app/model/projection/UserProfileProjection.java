package app.model.projection;

import java.time.LocalDateTime;
import java.util.UUID;

import app.model.custom.UserRole;

public interface UserProfileProjection {
    UUID getId();
    String getUsername();
    String getEmail();
    String getFullName();
    String getGender();
    String getProfilePicture();
    String getPhoneNumber();
    UserRole getRole();
    LocalDateTime getCreatedAt();
}
