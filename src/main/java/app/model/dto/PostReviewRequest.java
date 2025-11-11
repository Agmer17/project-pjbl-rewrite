package app.model.dto;

import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostReviewRequest {

    @NotNull(message = "id gak boleh kosong")
    private UUID productId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    @NotNull
    @NotBlank(message = "text review tidak boleh kosong")
    private String textReview;
    
}
