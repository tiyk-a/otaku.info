package otaku.info.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

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
}
