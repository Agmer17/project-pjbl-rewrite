package app.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import app.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class OptionalJwtSessionInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // kalau udah ada session dan creds, lanjut aja
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("creds") != null) {
            return true;
        }

        // cari cookie access token
        String cookieValue = null;
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("AccessToken".equalsIgnoreCase(c.getName())) {
                    cookieValue = c.getValue();
                    break;
                }
            }
        }

        // kalau gak ada cookie, lanjut aja (skip)
        if (cookieValue == null || cookieValue.isBlank()) {
            return true;
        }

        // coba parse jwt nya
        try {
            Claims claims = jwtUtils.parseToken(cookieValue);
            request.getSession(true).setAttribute("creds", claims);
        } catch (JwtException e) {
            // kalau token invalid atau expired, yaudah lanjut aja, jangan throw atau
            // redirect
            System.out.println("[OptionalJwtSessionInterceptor] Token invalid/expired, lanjut tanpa session");
        }

        return true; // lanjut ke handler berikutnya
    }

}
