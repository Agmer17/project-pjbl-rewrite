package app.controller;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import app.event.OnlineUsersListener;
import app.model.custom.UserRole;
import app.model.dto.ChatHistoryDto;
import app.model.dto.ChatListDto;
import app.model.dto.ChatMessage;
import app.model.dto.LiveChatResponseDto;
import app.model.entity.Product;
import app.model.entity.ProductProjection;
import app.service.LiveChatService;
import app.service.ProductService;
import io.jsonwebtoken.Claims;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
/**
 * Class: LivechatController
 * ---------------------------------
 * Hubungan dan konsep OOP yang digunakan:
 *
 * 1. Asosiasi :
 * - LivechatController berasosiasi dengan LiveChatService melalui @Autowired.
 * Controller menggunakan service untuk menyimpan dan mengambil data chat.
 * - Juga berasosiasi dengan SimpMessagingTemplate untuk mengirim pesan
 * real-time.
 *
 * 2. Relasi dengan LiveChat & Users:
 * - Melalui LiveChatService, controller berinteraksi dengan LiveChat dan Users.
 * - LiveChatService sudah menangani komposisi LiveChat → Users (cascade delete,
 * Many-to-One).
 * - Controller tidak mengubah hubungan database secara langsung, hanya
 * memanggil service.
 *
 * 3. Pola desain:
 * - Controller bertanggung jawab untuk request mapping dan response (MVC
 * pattern).
 * - Hubungan ke service menunjukkan dependency injection → asosiasi lemah.
 * - Tidak ada inheritance yang digunakan di class ini.
 */
public class LivechatController {

        @Autowired
        private ProductService productService;

        @Autowired
        private SimpMessagingTemplate messagingTemplate;

        @Autowired
        private LiveChatService service;

        @Autowired
        private OnlineUsersListener onlineUsersListener;

        @MessageMapping("/chat")
        public void processMessage(@Payload ChatMessage messagePayload,
                        SimpMessageHeaderAccessor headerAccessor) {

                UUID senderId = UUID.fromString((String) headerAccessor.getSessionAttributes().get("id"));
                UUID receiverId = messagePayload.getReceiverId();

                LiveChatResponseDto responseForReceiver = service.saveMessage(messagePayload, senderId, receiverId);

                LiveChatResponseDto responseForSender = responseForReceiver
                                .toBuilder()
                                .ownMessage(true)
                                .build();
                // Kirim ke dua user
                messagingTemplate.convertAndSendToUser(receiverId.toString(), "/queue/messages", responseForReceiver);
                messagingTemplate.convertAndSendToUser(senderId.toString(), "/queue/messages", responseForSender);
        }

        @GetMapping("/live-chat/with/{receiverId}")
        public String personalLiveChat(
                        @SessionAttribute Claims creds,
                        @PathVariable UUID receiverId,
                        Model model,
                        @ModelAttribute(name = "initialProduct") ProductProjection product) {

                UUID userId = UUID.fromString(creds.get("id", String.class));

                ChatHistoryDto data = service.getHistoryBeetween(userId, receiverId);

                model.addAttribute("data", data);
                model.addAttribute("userId", userId);
                model.addAttribute("receiverId", receiverId);
                model.addAttribute("initialProduct", product);

                System.out.println("\n\n\n\n\n\n\n" + product.getName());

                return "privateChatPage";
        }

        @GetMapping("/live-chat")
        public String getLiveChatPage(@SessionAttribute Claims creds, Model model) {
                UUID userId = UUID.fromString(creds.get("id", String.class));
                UserRole role = UserRole.valueOf(creds.get("role", String.class));

                List<ChatListDto> chList = service.getChatList(userId, role);

                model.addAttribute("chatList", chList);
                model.addAttribute("userId", userId);

                return "liveChatPage";

        }

        @GetMapping("/live-chat/online-users")
        @ResponseBody
        public Set<UUID> getOnlineUsers() {
                return onlineUsersListener.getOnlineUserIds();
        }

        @GetMapping("/livechat/redirect/p/{productId}/u/{userId}")
        public String getMethodName(@PathVariable UUID productId,
                        @PathVariable UUID userId,
                        RedirectAttributes redAtrs) {
                System.out.println("\n\n\n\n\n" + productId + " admin : " + " " + userId);
                redAtrs.addFlashAttribute("initialProduct", productService.findProductPreviewId(productId));
                return "redirect:/live-chat/with/" + userId;
        }

        @PostMapping("/live-chat/delete/{chatId}")
        @ResponseBody
        public ResponseEntity<?> deleteOwnMessage(@PathVariable UUID chatId, @SessionAttribute Claims creds) {
                UUID userId = UUID.fromString(creds.get("id", String.class));

                ResponseEntity<?> resp = service.deleteMessage(chatId, userId);
                return resp;
        }

}
