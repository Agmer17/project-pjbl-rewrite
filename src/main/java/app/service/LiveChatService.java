package app.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.model.custom.UserRole;
import app.model.dto.ChatHistoryDto;
import app.model.dto.ChatListDto;
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
                .createdAt(LocalDateTime.now())
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
                        .build())
                .collect(Collectors.toList());

        UserProfileProjection senderData = userRepository.findProfileById(sender).get();
        UserProfileProjection receiverData = userRepository.findProfileById(receiver).get();

        return ChatHistoryDto
                .builder()
                .currentUser(senderData)
                .receiver(receiverData)
                .chatHistory(chatHistoryData).build();
    }

    public List<ChatListDto> getChatList(UUID currentUserId, UserRole role) {

        if (role == UserRole.ADMIN) {

            List<ChatListDto> chatList = chatRepository.findChatListByUser(currentUserId);

            if (chatList == null || chatList.isEmpty()) {

                return List.of();
            }

            return chatList;

        } else {
            List<ChatListDto> chats = chatRepository.findChatListWithAdmin(currentUserId);
            List<Users> allAdmins = userRepository.findAllByRole(UserRole.ADMIN);

            Set<UUID> chattedAdminIds = chats.stream()
                    .map(ChatListDto::getUserId)
                    .collect(Collectors.toSet());

            for (Users admin : allAdmins) {
                if (!chattedAdminIds.contains(admin.getId())) {
                    chats.add(new ChatListDto(
                            admin.getId(),
                            admin.getFullName(),
                            admin.getUsername(),
                            admin.getProfilePicture(),
                            admin.getRole(),
                            null, // belum ada pesan
                            null, // belum ada waktu
                            false,
                            false));
                }
            }

            chats.sort(Comparator.comparing(ChatListDto::getLastMessageTime,
                    Comparator.nullsLast(Comparator.reverseOrder())));

            return chats;

        }
    }
}
