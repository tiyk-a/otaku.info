package otaku.info.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;

/**
 * 関係のない商品を保存するテーブル
 *
 */
@Entity(name = "del_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "del_item")
public class DelItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long del_item_id;

    @Column(nullable = false)
    private int site_id;

    @Column(nullable = false)
    private String item_code;

    @Column(nullable = true)
    private String url;

    @Column(nullable = true)
    private int price;

    @Column(nullable = false)
    private int team_id;

    @Column(nullable = true)
    private int artist_id;

    @Column(nullable = true)
    private String title;

    @Column(nullable = false)
    private String item_caption;

    @Column(nullable = true)
    private Date publication_date;

    @CreationTimestamp
    @Column(nullable = true)
    private Timestamp created_at;

    @UpdateTimestamp
    @Column(nullable = true)
    private Timestamp updated_at;

    public Item convertToItem() {
        Item item = new Item();
        item.setTitle(title);
        item.setItem_caption(item_caption);
        item.setTeam_id(team_id);
        item.setItem_code(item_code);
        item.setUrl(url);
        item.setPrice(price);
        item.setSite_id(site_id);
        item.setArtist_id(artist_id);

        return item;
    }
}
