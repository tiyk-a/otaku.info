package otaku.info.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * otakuinfoと関係なし
 * Youtube実験用のエンティティ
 *
 */
@Entity(name = "message")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "message")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long message_id;

    @Column(nullable = false)
    private String title;

    @CreationTimestamp
    @Column(nullable = true)
    private Timestamp created_at;
}
