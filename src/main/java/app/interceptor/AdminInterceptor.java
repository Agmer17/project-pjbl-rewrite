package app.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import app.model.custom.UserRole;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        Claims creds = (Claims) request.getSession(false).getAttribute("creds");

        UserRole currentUserRole = UserRole.valueOf(creds.get("role", String.class));

        if (!(currentUserRole == UserRole.ADMIN)) {
            response.sendRedirect("/");
            return false;
        }

        return true;
    }
    
}
