package app.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.model.dto.ChatHistoryDto;
import app.model.dto.ChatMessage;
import app.model.dto.LiveChatResponseDto;
import app.model.entity.LiveChat;
import app.model.entity.Users;
import app.model.projection.UserProfileProjection;
import app.repository.LiveChatRepository;
import app.repository.UserRepository;

@Service
public class LiveChatService {

    @Autowired
    private LiveChatRepository chatRepository;

    @Autowired
    private UserRepository userRepository;

    public LiveChatResponseDto saveMessage(ChatMessage message, UUID senderId, UUID receiverId) {

        Users sender = userRepository.getReferenceById(senderId);
        Users receiver = userRepository.getReferenceById(receiverId);

        LiveChat chat = LiveChat.builder()
                .sender(sender)
                .receiver(receiver)
                .text(message.getText())
                .haveRead(false)
                .build();

        chatRepository.save(chat);

        // Buat DTO yang sama, tapi ownMessage-nya beda
        LiveChatResponseDto responseForReceiver = LiveChatResponseDto.builder()
                .chatId(chat.getId())
                .sender(senderId)
                .receiver(receiverId)
                .text(chat.getText())
                .timeStamp(chat.getCreatedAt())
                .ownMessage(false)
                .build();

        return responseForReceiver;

    }

    public ChatHistoryDto getHistoryBeetween(UUID sender, UUID receiver) {

        List<LiveChat> chats = chatRepository.findChatBetweenUsers(sender, receiver);

        List<LiveChatResponseDto> chatHistoryData = chats.stream()
        .map(ch -> LiveChatResponseDto.builder()
                .chatId(ch.getId())
                .sender(ch.getSender().getId())
                .receiver(ch.getReceiver().getId())
                .text(ch.getText())
                .timeStamp(ch.getCreatedAt())
                .ownMessage(ch.getSender().getId().equals(sender))
                .build()
        )
        .collect(Collectors.toList());

        UserProfileProjection senderData = userRepository.findProfileById(sender).get();
        UserProfileProjection  receiverData = userRepository.findProfileById(receiver).get();

        return ChatHistoryDto
        .builder()
        .currentUser(senderData)
        .receiver(receiverData)
        .chatHistory(chatHistoryData).build();
    }
}
