package otaku.info.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * BlogTagテーブル(Wp tag)
 *
 */
@Entity(name = "blog_tag")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "blog_tag")
public class BlogTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long blog_tag_id;

    @Column(nullable = false)
    private Long wp_tag_id;

    @Column(nullable = true)
    private String tag_name;

    @Column(nullable = true)
    private String link;

    @Column(nullable = false)
    private Long team_id;

    @CreationTimestamp
    @Column(nullable = true)
    private Timestamp created_at;

    @UpdateTimestamp
    @Column(nullable = true)
    private Timestamp updated_at;
}
