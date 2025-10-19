package app.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import app.model.entity.LiveChat;

@Repository
public interface LiveChatRepository extends JpaRepository<LiveChat, UUID> {



    @Query("""
                SELECT lc FROM LiveChat lc
                WHERE (lc.sender.id = :sender AND lc.receiver.id = :receiver)
                   OR (lc.sender.id = :receiver AND lc.receiver.id = :sender)
                ORDER BY lc.createdAt ASC
            """)
    List<LiveChat> findChatBetweenUsers(
            @Param("sender") UUID sender,
            @Param("receiver") UUID receiver);
}
