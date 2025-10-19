package app.model.dto;

import java.util.List;

import app.model.projection.UserProfileProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatHistoryDto {
    private UserProfileProjection currentUser;
    private UserProfileProjection receiver;
    private List<LiveChatResponseDto> chatHistory;
}
