package app.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.model.custom.UserRole;
import app.model.entity.Users;
import app.model.projection.UserProfileProjection;



@Repository
public interface UserRepository extends JpaRepository<Users, UUID> {
    
    Optional<Users> findByUsername(String username);
    List<Users> findAllByUsernameOrEmail(String username, String email);
    Optional<Users> findByUsernameOrEmail(String username, String email);
    Optional<UserProfileProjection> findProfileById(UUID id);
    List<Users> findAllByRole(UserRole role);
    Page<UserProfileProjection> findAllProjectedBy(Pageable pageable);
}
