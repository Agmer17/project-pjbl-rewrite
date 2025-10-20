package app.model.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import app.model.custom.UserRole;
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
public class ChatListDto {

    private UUID userId;
    private String fullName;
    private String username;
    private String profilePicture;
    private UserRole role;
    
    // Chat terakhir
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Boolean isRead;
    private Boolean isSender;
    
}
