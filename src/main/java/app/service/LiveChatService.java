package app.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import app.event.OnlineUsersListener;
import app.exception.DataNotFoundEx;
import app.model.custom.UserRole;
import app.model.dto.ChatHistoryDto;
import app.model.dto.ChatListDto;
import app.model.dto.ChatMessage;
import app.model.dto.LiveChatResponseDto;
import app.model.dto.ProductChatDto;
import app.model.entity.LiveChat;
import app.model.entity.Product;
import app.model.entity.Users;
import app.model.projection.UserProfileProjection;
import app.repository.LiveChatRepository;
import app.repository.UserRepository;
import jakarta.transaction.Transactional;

@Service
/**
 * Class: LiveChatService
 * ---------------------------------
 * Hubungan dan konsep OOP yang digunakan:
 *
 * 1. Asosiasi:
 * - LiveChatService berasosiasi dengan LiveChatRepository dan UserRepository.
 * - LiveChatService menggunakan kedua repository ini untuk menyimpan dan
 * mengambil data.
 * - Hubungan ini bersifat "has-a" / dependency injection (DI)
 * melalui @Autowired.
 *
 * 2. Relasi dengan LiveChat:
 * - Menggunakan class LiveChat untuk membuat, menyimpan, dan mengambil pesan.
 * - LiveChatService tidak meng-extend LiveChat → bukan inheritance.
 * - LiveChat yang dibuat berisi reference ke Users (sender & receiver), yang
 * merupakan komposisi.
 *
 * 3. Relasi dengan Users:
 * - Mengambil Users dari UserRepository untuk menentukan pengirim dan penerima
 * pesan.
 * - LiveChatService tidak menyimpan Users sendiri, tapi menggunakan Users untuk
 * membangun LiveChat.
 * - Hubungan ini tetap mengikuti komposisi dari LiveChat ke Users (cascade
 * delete ada di LiveChat class).
 *
 * 4. DTO dan Projections:
 * - LiveChatResponseDto, ChatHistoryDto, dan ChatListDto digunakan untuk
 * transfer data.
 * - Ini tidak mengubah hubungan database, tapi menyederhanakan data yang
 * dikirim ke client.
 *
 * 5. Pola desain:
 * - Service ini bertanggung jawab atas logika bisnis chat.
 * - Repository bertindak sebagai abstraksi database (Spring Data JPA).
 * - Dependency Injection (@Autowired) menunjukan asosiasi
 */
public class LiveChatService {

    @Autowired
    private LiveChatRepository chatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private OnlineUsersListener onlineListener;

    @Autowired
    private ProductService productService;

    @Transactional
    public LiveChatResponseDto saveMessage(ChatMessage message, UUID senderId, UUID receiverId) {

        Users sender = userRepository.getReferenceById(senderId);
        Users receiver = userRepository.getReferenceById(receiverId);

        Product product = null;
        ProductChatDto productChatDto = null;

        if (message.getProductId() != null) {
            product = productService.getProductDetails(message.getProductId());

            productChatDto = ProductChatDto.fromEntity(product);

        }

        LiveChat chat = LiveChat.builder()
                .sender(sender)
                .receiver(receiver)
                .text(message.getText())
                .createdAt(LocalDateTime.now())
                .haveRead(false)
                .product(product)
                .build();

        chatRepository.save(chat);

        // Buat DTO yang sama, tapi ownMessage-nya beda
        LiveChatResponseDto responseForReceiver = LiveChatResponseDto.builder()
                .chatId(chat.getId())
                .sender(senderId)
                .receiver(receiverId)
                .text(chat.getText())
                .product(productChatDto)
                .timeStamp(chat.getCreatedAt())
                .ownMessage(false)
                .build();

        if (!isReceiverOnline(receiver)) {
            emailService.sendNotificationEmails("http://localhost/live-chat", receiver.getEmail(),
                    sender.getUsername());
        }
        return responseForReceiver;

    }

    public ChatHistoryDto getHistoryBeetween(UUID sender, UUID receiver) {

        List<LiveChat> chats = chatRepository.findChatBetweenUsers(sender, receiver);

        List<LiveChatResponseDto> chatHistoryData = chats.stream()
                .map(ch -> LiveChatResponseDto.builder()
                        .chatId(ch.getId())
                        .sender(ch.getSender().getId())
                        .receiver(ch.getReceiver().getId())
                        .timeStampEpoch(ch.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                        .text(ch.getText())
                        .timeStamp(ch.getCreatedAt())
                        .ownMessage(ch.getSender().getId().equals(sender))
                        .product(ProductChatDto.fromEntity(ch.getProduct()))
                        .build())
                .collect(Collectors.toList());

        UserProfileProjection senderData = userRepository.findProfileById(sender).orElse(null);
        UserProfileProjection receiverData = userRepository.findProfileById(receiver).orElse(null);

        if (senderData == null || receiverData == null) {

            throw new DataNotFoundEx("Akun kamu mungkin telah terhapus, silahkan login ulang", "/login");

        }

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

    private Boolean isReceiverOnline(Users u) {
        return onlineListener.getOnlineUserIds().contains(u.getId());

    }

    @Transactional
    public ResponseEntity<?> deleteMessage(UUID chatId, UUID userId) {

        LiveChat deletedChat = chatRepository.findById(chatId).orElse(null);

        if (deletedChat == null) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        if (!deletedChat.getSender().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        chatRepository.delete(deletedChat);

        return ResponseEntity.ok().build();

    }
}
