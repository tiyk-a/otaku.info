package otaku.info.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import otaku.info.form.IMForm;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;

/**
 * ItemMaster
 * 同じ商品について複数の販売者・リンクがあることが多いので
 * １つにまとめるマスタ商品テーブル
 *
 */
@Entity(name = "item_master")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "item_master")
public class ItemMaster implements Comparable<ItemMaster> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long item_m_id;

    @Column(nullable = true)
    private String url;

    @Column(nullable = true)
    private String title;

//    @Column(nullable = true)
//    private Long team_id;

    @Column(nullable = true)
    private Long wp_id;

    @Column(nullable = true)
    private String item_caption;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(nullable = true)
    private Date publication_date;

    @Column(nullable = true)
    private Integer price;

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

    @CreationTimestamp
    @Column(nullable = true)
    private Timestamp created_at;

    @UpdateTimestamp
    @Column(nullable = true)
    private Timestamp updated_at;

    // compareTo method to sort in
    // ascending order
    public int compareTo(ItemMaster obj)
    {
        long res = this.item_m_id - obj.item_m_id;
        return (int) res;
    }

    public boolean isNull() {
        return this.getItem_m_id() == null;
    }

    /**
     * 空のImageカラムがあるか確認し、一番初めの空のカラムに引数の画像パスを設定します。
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

    public boolean isNewImage(String imageUrl) {
        if (!StringUtils.hasText(this.getImage1())) {
            return true;
        } else if (this.getImage1().equals(imageUrl)) {
            return false;
        } else if (!StringUtils.hasText(this.getImage2())) {
            return true;
        } else if (this.getImage2().equals(imageUrl)) {
            return false;
        } else return !StringUtils.hasText(this.getImage3());
    }

    public ItemMaster adjustedCopy() {
        ItemMaster newItemMaster = new ItemMaster();
        BeanUtils.copyProperties(this, newItemMaster);
        newItemMaster.setItem_m_id(null);
        newItemMaster.setFct_chk(false);
        newItemMaster.setCreated_at(null);
        newItemMaster.setUpdated_at(null);
        return newItemMaster;
    }

    /**
     * formに値があればそれを取り込む
     * TODO: fct_chk, del_flgは未対応
     *
     * @param imForm
     * @return
     */
    public ItemMaster absorb(IMForm imForm) {
        if (!imForm.getTitle().isEmpty()) {
            this.setTitle(imForm.getTitle());
        }
        if (!imForm.getItem_caption().isEmpty()) {
            this.setItem_caption(imForm.getItem_caption());
        }
        if (imForm.getPrice() != null && imForm.getPrice() != 0) {
            this.setPrice(imForm.getPrice());
        }
        if (imForm.getPublication_date() != null) {
            this.setPublication_date(imForm.getPublication_date());
        }
        if (!imForm.getUrl().isEmpty()) {
            this.setUrl(imForm.getUrl());
        }
        return this;
    }
}
