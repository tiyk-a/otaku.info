package otaku.info.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * 楽天ROOMで使う
 * 私と関わったアカウントの管理
 */
@Entity(name = "room_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "room_user")
public class RoomUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String user_id;

    @Column(nullable = true)
    private String username;

    @Column(nullable = false)
    private Boolean follow_me;

    @Column(nullable = false)
    private Boolean follow;

    @Column(nullable = false)
    private Boolean is_recorrer;

    @Column(nullable = false)
    private Integer like_count;

    @Column(nullable = false)
    private String user_rank;

    @CreationTimestamp
    @Column(nullable = true)
    private Timestamp created_at;

    @UpdateTimestamp
    @Column(nullable = true)
    private Timestamp updated_at;
}
