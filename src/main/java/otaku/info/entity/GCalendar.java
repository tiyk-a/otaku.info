package otaku.info.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Google Calendarの管理
 *
 */
@Entity(name = "g_calendar")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "g_calendar")
public class GCalendar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long g_calendar_id;

    /**
     * そのGooglebCalendarの予定
     */
    @Column(nullable = false)
    private String event_id;

    /**
     * TeamEnumに繋いでcalendarIdを取得できる
     */
    @Column(nullable = true)
    private Long team_id;

    @Column(nullable = true)
    private String member_arr;

    /**
     * BlogPostのID
     */
    @Column(nullable = false)
    private Long blog_post_id;

    @Column(columnDefinition = "Boolean default false")
    private Boolean del_flg;

    /** イベントのカテゴリ、内部管理用。1=商品、10=TVとしようかな。商品は細分化される予定 */
    @Column(nullable = false)
    private Long category_id;

    @CreationTimestamp
    @Column(nullable = true)
    private Timestamp created_at;

    @UpdateTimestamp
    @Column(nullable = true)
    private Timestamp updated_at;
}
