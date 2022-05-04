package otaku.info.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * DelCalテーブル
 * 削除されたGoogle Calendarイベントデータを保持する
 *
 */
@Entity(name = "del_cal")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "del_cal")
public class DelCal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long del_cal_id;

    @Column(nullable = false)
    private String calendarId;

    @Column(nullable = false)
    private Long teamId;

    @Column(nullable = false)
    private String eventId;

    /** イベントのカテゴリ。1=商品、10=TVとしようかな。商品は細分化される予定 */
    @Column(nullable = false)
    private Long categoryId;

    @CreationTimestamp
    @Column(nullable = true)
    private Timestamp created_at;

    @UpdateTimestamp
    @Column(nullable = true)
    private Timestamp updated_at;
}
