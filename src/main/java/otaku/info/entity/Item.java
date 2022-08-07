package otaku.info.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    /** 楽天=1, Yahoo=2 */
    @Column(nullable = false)
    private Integer site_id;

    @Column(nullable = false)
    private String item_code;

    @Column(nullable = true)
    private String url;

    @Column(nullable = true)
    private Integer price;

    @Column(nullable = true)
    private String title;

    @Column(nullable = true)
    private String item_caption;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(nullable = true)
    private Date publication_date;

    @Column(nullable = false)
    private boolean fct_chk;

    @Column(nullable = false)
    private boolean del_flg;

    @Column(nullable = true)
    private Long im_id;

    @Column(nullable = true)
    private String teamArr;

    @Column(nullable = true)
    private String memArr;

    @CreationTimestamp
    @Column(nullable = true)
    private Timestamp created_at;

    @UpdateTimestamp
    @Column(nullable = true)
    private Timestamp updated_at;

    public List<Item> toList() {
        List<Item> itemList = new ArrayList<>();
        itemList.add(this);
        return itemList;
    }

    public Item absorb(Item preyItem) {
        if (preyItem.getTitle() != null && !preyItem.getTitle().isEmpty()) {
            this.setTitle(preyItem.getTitle());
        }
        if (preyItem.getItem_caption() != null && !preyItem.getItem_caption().isEmpty()) {
            this.setItem_caption(preyItem.getItem_caption());
        }
        if (preyItem.getPrice() != null && preyItem.getPrice() != 0) {
            this.setPrice(preyItem.getPrice());
        }
        if (preyItem.getPublication_date() != null) {
            this.setPublication_date(preyItem.getPublication_date());
        }
        return this;
    }
}
