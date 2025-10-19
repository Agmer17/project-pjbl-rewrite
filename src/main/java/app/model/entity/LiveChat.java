package app.model.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.DynamicInsert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@DynamicInsert
public class LiveChat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false)
    private UUID id;

    // ✅ HAPUS @Column, tipe data jadi User (bukan UUID)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false, 
                foreignKey = @ForeignKey(name = "live_chat_sender_id_fkey"))
    private Users sender;  // ✅ User, bukan UUID

    // ✅ HAPUS @Column, tipe data jadi User (bukan UUID)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false, 
                foreignKey = @ForeignKey(name = "live_chat_receiver_id_fkey"))
    private Users receiver;  // ✅ User, bukan UUID
   
    @Column(columnDefinition = "TEXT", name = "message")
    private String text;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "is_read")
    private Boolean haveRead;
}
