package app.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import app.model.entity.PasswordResetToken;

public interface PasswordResetTokenRepo extends JpaRepository<PasswordResetToken, UUID> {

    
    Optional<PasswordResetToken> findByTokenAndExpiresAtAfterAndUsedFalse(String token, LocalDateTime now);
    
}
