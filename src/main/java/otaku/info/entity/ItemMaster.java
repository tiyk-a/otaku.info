package otaku.info.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

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
public class ItemMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long item_m_id;

    @Column(nullable = true)
    private Integer wp_id;

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

    public boolean isNull() {
        return this.getItem_m_id() == null;
    }
}
