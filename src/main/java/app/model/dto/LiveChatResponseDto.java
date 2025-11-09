package app.model.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class LiveChatResponseDto {
    private UUID chatId;
    private UUID sender;
    private UUID receiver;
    private String text;
    private LocalDateTime timeStamp;
    private Long timeStampEpoch;
    private Boolean ownMessage;
}
