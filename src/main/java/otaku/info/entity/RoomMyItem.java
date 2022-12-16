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
 * 私のItem管理マスタ
 * 毎日、ここにデータあるItemについて管理する
 */
@Entity(name = "room_my_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "room_my_item")
public class RoomMyItem {

    /**
     * 楽天ROOMのItemIdをそのまま使う
     */
    @Id
    private String item_id;

    /**
     * 毎日更新、その日のいいね数を入れる
     */
    @Column(nullable = false)
    private Integer likes;

    @Column(nullable = false)
    private String postedDate;

    /**
     * 前回バッチからのいいね変動数
     */
    @Column(nullable = false)
    private Integer newLikeCount;

    @CreationTimestamp
    @Column(nullable = true)
    private Timestamp created_at;

    @UpdateTimestamp
    @Column(nullable = true)
    private Timestamp updated_at;
}
