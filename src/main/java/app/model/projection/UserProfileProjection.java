package app.model.projection;

import java.time.LocalDateTime;
import java.util.UUID;

public interface UserProfileProjection {
    UUID getId();
    String getUsername();
    String getEmail();
    String getFullName();
    String getGender();
    String getProfilePicture();
    String getPhoneNumber();
    LocalDateTime getCreatedAt();
}
