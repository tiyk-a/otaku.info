package otaku.info.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;

/**
 * 商品テーブル
 *
 */
@Entity(name = "Item")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Item")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long item_id;

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

    @Column(nullable = true)
    private String item_caption;

    @Column(nullable = true)
    private Date publication_date;

    @CreationTimestamp
    @Column(nullable = true)
    private Timestamp created_at;

    @UpdateTimestamp
    @Column(nullable = true)
    private Timestamp updated_at;

//    public DelItem convertToDelItem() {
//        DelItem delItem = new DelItem();
//        delItem.setArtist_id(artist_id);
//        delItem.setItem_caption(item_caption);
//        delItem.setItem_code(item_code);
//        delItem.setPrice(price);
//        delItem.setPublication_date(publication_date);
//        delItem.setSite_id(site_id);
//        delItem.setTeam_id(team_id);
//        delItem.setTitle(title);
//        delItem.setUrl(url);
//        return delItem;
//    }
}
