package otaku.info.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * BlogUpdテーブル
 * created_atとupdated_atの日付が異なる→更新済み、と判定可能
 *
 */
@Entity(name = "blog_upd")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "blog_upd")
public class BlogUpd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long blog_upd_id;

    @Column(nullable = false)
    private Long im_id;

    @CreationTimestamp
    @Column(nullable = true)
    private Timestamp created_at;

    @UpdateTimestamp
    @Column(nullable = true)
    private Timestamp updated_at;
}
