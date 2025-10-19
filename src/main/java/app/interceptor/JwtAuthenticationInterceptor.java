package app.interceptor;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.support.RequestContextUtils;

import app.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


@Component
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession currentSession = request.getSession(false);

        // kalo udah ada session langsung lanjut ke handler berikutnya
        if (currentSession != null && currentSession.getAttribute("creds") != null) {
            return true;
        }

        if (request.getCookies() == null) {
            response.setStatus(401);
            redirectWithFlash(request, response, "/login", "warningMessage",
                    "untuk mengakses fitur ini silahkan login dulu");

            return false;
        }

        String cookie = null;
        for (Cookie c : request.getCookies()) {
            if ("AccessToken".equalsIgnoreCase(c.getName())) {
                cookie = c.getValue();
            }
        }

        if (cookie == null || cookie.isEmpty() || cookie.isBlank()) {
            response.setStatus(401);
            redirectWithFlash(request, response, "/login", "warningMessage",
                    "untuk mengakses fitur ini silahkan login dulu");
            return false;
        }

        try {
            Claims claims = jwtUtils.parseToken(cookie);
            request.getSession(true).setAttribute("creds", claims);
            return true;
        } catch (ExpiredJwtException e) {
            redirectWithFlash(request, response, "/login", "warningMessage", "sesi telah habis, silahkan login kembali");
            response.sendRedirect("/login");
            return false;
        } catch (JwtException e) {
            redirectWithFlash(request, response, "/login", "warningMessage", "sesi telah habis, silahkan login kembali");
            response.sendRedirect("/login");
            return false;
        }

    }

    private void redirectWithFlash(HttpServletRequest request, HttpServletResponse response,
            String redirectUrl, String key, String message) throws IOException {
        FlashMap flashMap = new FlashMap();
        flashMap.put(key, message);

        FlashMapManager flashMapManager = RequestContextUtils.getFlashMapManager(request);
        if (flashMapManager != null) {
            flashMapManager.saveOutputFlashMap(flashMap, request, response);
        }

        response.sendRedirect(redirectUrl);
    }

}
