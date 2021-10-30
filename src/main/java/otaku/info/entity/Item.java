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

    @Column(nullable = false)
    private int site_id;

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

    @Column(nullable = true)
    private String image1;

    @Column(nullable = true)
    private String image2;

    @Column(nullable = true)
    private String image3;

    @Column(nullable = false)
    private boolean fct_chk;

    @Column(nullable = false)
    private boolean del_flg;

    @Column(nullable = true)
    private Long im_id;

    @CreationTimestamp
    @Column(nullable = true)
    private Timestamp created_at;

    @UpdateTimestamp
    @Column(nullable = true)
    private Timestamp updated_at;

    /**
     * Itemに空のImageカラムがあるか確認し、一番初めの空のカラムに引数の画像パスを設定します。
     * まだ空のカラムが残っている場合はTを、fullで登録できなかった/登録してfullになった場合はFを返します。
     * また、引数が空の場合はFalseを返すため、item/itemMaster間で使うときはimage1->3の順で使うべし。
     *
     * @param imageUrl
     */
    public boolean fillBlankImage(String imageUrl) {

        // もし引数が空だったらfalseを返す
        if (imageUrl == null || imageUrl.equals("")) {
            return false;
        }

        // image1が空だったら入れる
        if (this.getImage1() == null) {
            this.setImage1(imageUrl);
            return true;
        } else if (this.getImage1().equals(imageUrl)) {
            // image1と引数が同じ値だったら、image2/3に入れてはいけないのでimage2/3が空かどうかでreturn値を決める
            return this.getImage2() == null || this.getImage3() == null;
        } else if (this.getImage2() == null) {
            // image2が空だったら入れる
            this.setImage2(imageUrl);
            return true;
        } else if (this.getImage2().equals(imageUrl)) {
            // image2と引数が同じ値だったら、image3に入れてはいけないのでimage3が空かどうかでreturn値を決める
            return this.getImage3() == null;
        } else if (this.getImage3() == null) {
            // image3が空だったら入れる
            this.setImage3(imageUrl);
            return false;
        } else {
            return false;
        }
    }

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
