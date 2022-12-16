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
 * 私のItemへのいいね管理
 * 1日1レコード、前日と比べ数が変更あったら追加する
 *
 */
@Entity(name = "room_item_like")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "room_item_like")
public class RoomItemLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * room_my_itemのIDを入れる
     */
    @Column(nullable = false)
    private String item_id;

    /**
     * 前日のレコードと比べて増えたユーザーIDをカンマ区切りで
     */
    @Column(columnDefinition = "TEXT", nullable = true)
    private String added_user;

    /**
     * 前日のレコードと比べて減ったユーザーIDをカンマ区切りで
     */
    @Column(columnDefinition = "TEXT", nullable = true)
    private String minus_user;

    @CreationTimestamp
    @Column(nullable = true)
    private Timestamp created_at;

    @UpdateTimestamp
    @Column(nullable = true)
    private Timestamp updated_at;
}
