package app.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import app.exception.AuthValidationException;
import app.model.custom.Gender;
import app.model.custom.UserRole;
import app.model.dto.LoginRequest;
import app.model.dto.SignUpRequest;
import app.model.dto.UpdatePasswordRequest;
import app.model.entity.PasswordResetToken;
import app.model.entity.Users;
import app.repository.PasswordResetTokenRepo;
import app.repository.UserRepository;
import app.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

import java.util.Base64;
import java.security.SecureRandom;

@Service
public class AuthService {

    @Autowired
    private UserRepository repo;

    @Autowired
    JwtUtils jwtUtil;

    private SecureRandom secureRandom = new SecureRandom();

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordResetTokenRepo pwResetRepo;

    public void loginService(LoginRequest loginRequest, HttpServletResponse response, HttpServletRequest request) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        Optional<Users> optionalUsers = repo.findByUsername(username);

        if (optionalUsers.isPresent()) {
            Users user = optionalUsers.get();

            if (BCrypt.checkpw(password, user.getPassword())) {

                this.resetCurrentSession(request);

                String accessToken = jwtUtil.generateToken(user.getRole().toString(), user.getId());

                ResponseCookie cookie = ResponseCookie.from("AccessToken", accessToken).path("/").httpOnly(true)
                        .maxAge(Duration.ofDays(7)).sameSite("Strict").build();

                response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

                return;

            } else {
                throw new AuthValidationException("username atau password salah", "/login", loginRequest);
            }

        } else {
            throw new AuthValidationException("Akun tidak ditemukan", "/login", loginRequest);

        }
    }

    public void signUpRequest(SignUpRequest request) {
        Optional<Users> existingUser = repo.findByUsernameOrEmail(request.getUsername(), request.getEmail());

        existingUser.ifPresent(_ -> {
            if (existingUser.get().getUsername().equals(request.getUsername())) {
                throw new AuthValidationException("Username sudah digunakan", "/sign-up", request);
            }
            if (existingUser.get().getEmail().equals(request.getEmail())) {
                throw new AuthValidationException("Email sudah digunakan", "/sign-up", request);
            }
        });

        Users newUser = Users.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .role(UserRole.CUSTOMER)
                .gender(Gender.LAINNYA)
                .password(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt(12)))
                .build();

        repo.save(newUser);
    }

    public void logoutService(HttpServletRequest request, HttpServletResponse response) {
        this.resetCurrentSession(request);
        ResponseCookie deleteCookie = ResponseCookie.from("AccessToken", "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .sameSite("Strict")
                .build();

        response.setHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
    }

    private void resetCurrentSession(HttpServletRequest request) {
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
    }

    public void sendRequetsCode(String urlBase, String usernameOrEmail) {
        Users user = repo.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(
                        () -> new AuthValidationException("Username atau email tidak ditemukan", "/forgot-password", null));

        byte[] randomBytes = new byte[48];
        secureRandom.nextBytes(randomBytes);

        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(token)
                .expiresAt(LocalDateTime.now().plusMinutes(10)).build();

        pwResetRepo.save(resetToken);

        String resetLink = urlBase + "/reset-password?token=" + token;

        emailService.sendPasswordResetEmail(user.getEmail(), resetLink);

    }

    @Transactional
    public void updateUserPassword(UpdatePasswordRequest request) {
        String newPassword = request.getNewPassword();
        String token = request.getToken();
        LocalDateTime now = LocalDateTime.now();

        PasswordResetToken tokenMetadata = pwResetRepo.findByTokenAndExpiresAtAfterAndUsedFalse(token, now)
                .orElseThrow(() -> new AuthValidationException(
                        "Token tidak ditemukan atau sudah kadaluarsa",
                        "/reset-password", request));
        
        Users user = tokenMetadata.getUser();

        user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt(12)));
        tokenMetadata.setUsed(true);


        repo.save(user);
        pwResetRepo.save(tokenMetadata);
    }
}
