package otaku.info.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Wordpressブログを管理します
 */
@Entity(name = "blog_post")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "blog_post")
public class BlogPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long blog_post_id;

    /**
     * BlogEnumのID
     */
    @Column(nullable = false)
    private Long blog_enum_id;

    /**
     * blog_idが1グループのみのIDの場合でも
     * 複数グループにまたがるものなら、ここがarrayになる
     * →タグ作成に使う
     */
    @Column(nullable = true)
    private String team_arr;

    @Column(nullable = true)
    private String mem_arr;

    @Column(nullable = false)
    private Long im_id;

    @Column(nullable = true)
    private Long wp_id;

    /** WP blogにSEO対策のためにあげるJavaが生成した画像のパス(blog上) */
    @Column(nullable = true)
    private String inner_image;

    /** アイキャッチ画像のID */
    @Column(nullable = true)
    private Integer wp_eye_catch_id;

    @Column(columnDefinition = "Boolean default false")
    private Boolean del_flg;

    @CreationTimestamp
    @Column(nullable = true)
    private Timestamp created_at;

    @UpdateTimestamp
    @Column(nullable = true)
    private Timestamp updated_at;
}
