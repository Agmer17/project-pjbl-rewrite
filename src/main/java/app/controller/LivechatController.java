package app.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import app.model.dto.ChatHistoryDto;
import app.model.dto.ChatMessage;
import app.model.dto.LiveChatResponseDto;
import app.service.LiveChatService;
import io.jsonwebtoken.Claims;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
public class LivechatController {

        @Autowired
        private SimpMessagingTemplate messagingTemplate;

        @Autowired
        private LiveChatService service;

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
        public String personalLiveChat(@SessionAttribute Claims creds, @PathVariable UUID receiverId, Model model) {

                UUID userId = UUID.fromString(creds.get("id", String.class));

                ChatHistoryDto data = service.getHistoryBeetween(userId, receiverId);

                model.addAttribute("data", data);
                
                return "liveChatPage";
        }

}
