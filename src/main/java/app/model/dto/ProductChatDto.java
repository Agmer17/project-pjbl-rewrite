package app.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import app.model.entity.Product;
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
public class ProductChatDto {
    private UUID id;
    private String name;
    private String desc;
    private BigDecimal price;
    private String categoryName;
    private LocalDateTime createdAt;
    private String thumbnailUrl;
    private Long imageCount;

    public static ProductChatDto fromEntity(Product product) {
        if (product == null)
            return null;

        // Ambil kategori name jika ada
        String categoryName = product.getCategory() != null ? product.getCategory().getName() : null;

        // Ambil thumbnail (first image)
        String thumbnailUrl = null;
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            thumbnailUrl = product.getImages().get(0).getImageFileName();
        }

        // Hitung jumlah image
        Long imageCount = product.getImages() != null ? (long) product.getImages().size() : 0L;

        return ProductChatDto.builder()
                .id(product.getId())
                .name(product.getName())
                .desc(product.getDesc())
                .price(product.getPrice())
                .categoryName(categoryName)
                .createdAt(product.getCreatedAt())
                .thumbnailUrl(thumbnailUrl)
                .imageCount(imageCount)
                .build();
    }

}
