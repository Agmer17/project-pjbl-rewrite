package app.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import app.model.dto.ChatListDto;
import app.model.entity.LiveChat;

@Repository
/**
 * Interface: LiveChatRepository
 * ---------------------------------
 * Hubungan dan konsep OOP yang digunakan:
 *
 * 1. Inheritance:
 * - LiveChatRepository extends JpaRepository<LiveChat, UUID>
 * - Ini contoh inheritance di Java: repository mewarisi semua method CRUD dari
 * JpaRepository.
 *
 * 2. Asosiasi / Komposisi:
 * - Repository ini berasosiasi dengan class LiveChat.
 * - LiveChat sendiri memiliki reference ke Users (sender & receiver), yang
 * merupakan komposisi Many-to-One.
 *
 * 3. Relasi dengan Users:
 * - Custom query (findChatBetweenUsers, findChatListByUser,
 * findChatListWithAdmin)
 * memanfaatkan relasi LiveChat → Users.
 *
 * 4. Pola desain:
 * - Repository bertindak sebagai abstraksi database.
 * - Service menggunakan repository untuk implementasi logika bisnis.
 * - Hubungan ini menunjukkan dependency / asosiasi lemah, bukan komposisi
 * langsung.
 */
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

    // GUA GATAU NI KODE NGAPAIN, DAPET DARI AI. BUT SOMEHOW IT WORKS!
    @Query("""
                SELECT new app.model.dto.ChatListDto(
                    CASE
                        WHEN lc.sender.id = :currentUserId THEN lc.receiver.id
                        ELSE lc.sender.id
                    END,
                    CASE
                        WHEN lc.sender.id = :currentUserId THEN lc.receiver.fullName
                        ELSE lc.sender.fullName
                    END,
                    CASE
                        WHEN lc.sender.id = :currentUserId THEN lc.receiver.username
                        ELSE lc.sender.username
                    END,
                    CASE
                        WHEN lc.sender.id = :currentUserId THEN lc.receiver.profilePicture
                        ELSE lc.sender.profilePicture
                    END,
                    CASE
                        WHEN lc.sender.id = :currentUserId THEN lc.receiver.role
                        ELSE lc.sender.role
                    END,
                    lc.text,
                    lc.createdAt,
                    lc.haveRead,
                    (lc.sender.id = :currentUserId)
                )
                FROM LiveChat lc
                WHERE lc.createdAt = (
                    SELECT MAX(lc2.createdAt)
                    FROM LiveChat lc2
                    WHERE
                        (lc2.sender.id = lc.sender.id AND lc2.receiver.id = lc.receiver.id)
                        OR (lc2.sender.id = lc.receiver.id AND lc2.receiver.id = lc.sender.id)
                )
                AND (:currentUserId IN (lc.sender.id, lc.receiver.id))
                ORDER BY lc.createdAt DESC
            """)
    List<ChatListDto> findChatListByUser(@Param("currentUserId") UUID currentUserId);

    @Query("""
                SELECT new app.model.dto.ChatListDto(
                    CASE
                        WHEN lc.sender.id = :currentUserId THEN lc.receiver.id
                        ELSE lc.sender.id
                    END,
                    CASE
                        WHEN lc.sender.id = :currentUserId THEN lc.receiver.fullName
                        ELSE lc.sender.fullName
                    END,
                    CASE
                        WHEN lc.sender.id = :currentUserId THEN lc.receiver.username
                        ELSE lc.sender.username
                    END,
                    CASE
                        WHEN lc.sender.id = :currentUserId THEN lc.receiver.profilePicture
                        ELSE lc.sender.profilePicture
                    END,
                    CASE
                        WHEN lc.sender.id = :currentUserId THEN lc.receiver.role
                        ELSE lc.sender.role
                    END,
                    lc.text,
                    lc.createdAt,
                    lc.haveRead,
                    (lc.sender.id = :currentUserId)
                )
                FROM LiveChat lc
                WHERE lc.createdAt = (
                    SELECT MAX(lc2.createdAt)
                    FROM LiveChat lc2
                    WHERE
                        (lc2.sender.id = lc.sender.id AND lc2.receiver.id = lc.receiver.id)
                        OR (lc2.sender.id = lc.receiver.id AND lc2.receiver.id = lc.sender.id)
                )
                AND (:currentUserId IN (lc.sender.id, lc.receiver.id))
                AND (
                    (lc.sender.id = :currentUserId AND lc.receiver.role = 'ADMIN')
                    OR (lc.receiver.id = :currentUserId AND lc.sender.role = 'ADMIN')
                )
                ORDER BY lc.createdAt DESC
            """)
    List<ChatListDto> findChatListWithAdmin(@Param("currentUserId") UUID currentUserId);

}
