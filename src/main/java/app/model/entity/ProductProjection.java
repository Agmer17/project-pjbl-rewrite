package app.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

// ProductProjection.java
public interface ProductProjection {
    UUID getId();
    String getName();
    String getDesc();
    BigDecimal getPrice();
    String getCategoryName();
    LocalDateTime getCreatedAt();
    String getThumbnailUrl(); // URL dari ProductImage dengan galleryImage = false atau image pertama
    Long getImageCount();
}
