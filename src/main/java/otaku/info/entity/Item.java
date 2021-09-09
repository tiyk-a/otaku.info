package otaku.info.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

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
    private String team_id;

    @Column(nullable = true)
    private String member_id;

    @Column(nullable = true)
    private String title;

    @Column(nullable = true)
    private String item_caption;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(nullable = true)
    private Date publication_date;

    @Column(nullable = true)
    private String image1;

    @Column(nullable = true)
    private String image2;

    @Column(nullable = true)
    private String image3;

    @Column(nullable = true)
    private Integer wp_id;

    @Column(nullable = false)
    private boolean fct_chk;

    @Column(nullable = false)
    private boolean del_flg;

    @Column(nullable = true)
    private Long item_m_id;

    @CreationTimestamp
    @Column(nullable = true)
    private Timestamp created_at;

    @UpdateTimestamp
    @Column(nullable = true)
    private Timestamp updated_at;

    /**
     * マスター商品に変換します
     *
     * @return
     */
    public ItemMaster convertToItemMaster() {
        ItemMaster itemMaster = new ItemMaster();
        BeanUtils.copyProperties(this, itemMaster);
        itemMaster.setTitle(itemMaster.getTitle().replaceAll("(\\[.*?\\])|(\\/)|(【.*?】)|(\\(.*?\\))|(\\（.*?\\）)", ""));
        itemMaster.setFct_chk(false);
        itemMaster.setDel_flg(false);
        itemMaster.setItem_m_id(null);
        itemMaster.setUrl(null);
        itemMaster.setWp_id(null);
        return itemMaster;
    }
}
