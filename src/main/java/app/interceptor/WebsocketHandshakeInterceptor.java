package app.interceptor;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpSession;

@Component
public class WebsocketHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public void afterHandshake(ServerHttpRequest arg0, ServerHttpResponse arg1, WebSocketHandler arg2, Exception arg3) {
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
            Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpSession httpSession = servletRequest.getServletRequest().getSession(false);

            if (httpSession != null) {
                Claims claims = (Claims) httpSession.getAttribute("creds");

                if (claims == null) {
                    return false;
                }
                attributes.put("id", claims.get("id", String.class));
                attributes.put("role", claims.get("role", String.class));
            }
        }
        return true; // lanjut
    }
    
}
