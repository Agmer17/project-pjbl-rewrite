package app.event;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OnlineUsersListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final Set<UUID> onlineUsers = ConcurrentHashMap.newKeySet();

    @EventListener
    public void handleSessionConnected(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        UUID userId = UUID.fromString((String) accessor.getSessionAttributes().get("id"));

        if (userId != null) {
            onlineUsers.add(userId);
            messagingTemplate.convertAndSend("/topic/online-users", onlineUsers);
        }

        System.out.println("\n\n\n\n" + onlineUsers + "\n\n\n\n\n");
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        UUID userId = UUID.fromString((String) accessor.getSessionAttributes().get("id"));

        if (userId != null) {
            onlineUsers.remove(userId);
            messagingTemplate.convertAndSend("/topic/online-users", onlineUsers);
        }

        System.out.println("\n\n\n\n" + onlineUsers + "\n\n\n\n\n");

    }

    public Set<UUID> getOnlineUserIds() {
        return onlineUsers;
    }

}
