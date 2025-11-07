package app.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


public interface ReviewData {

    UUID getId();

    String getTextReview();

    Integer getRating();

    LocalDateTime getCreatedAt();

    String getStatus();

    UserInfo getUser();

    ProductInfo getProduct();

    // ========= USER ==========
    interface UserInfo {
        UUID getId();

        String getUsername();

        String getFullName();

        String getProfilePicture();

        String getRole();

        LocalDateTime getCreatedAt();

        String getGender();
    }

    // ========= PRODUCT ==========
    interface ProductInfo {
        UUID getId();

        String getName();

        String getDesc();

        BigDecimal getPrice();

        LocalDateTime getCreatedAt();
    }
}