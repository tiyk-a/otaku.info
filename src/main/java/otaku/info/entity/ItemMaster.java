package otaku.info.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import otaku.info.enums.WpTagEnum;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
    private String url;

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

    @CreationTimestamp
    @Column(nullable = true)
    private Timestamp created_at;

    @UpdateTimestamp
    @Column(nullable = true)
    private Timestamp updated_at;

    public boolean isNull() {
        return this.getItem_m_id() == null;
    }

    /**
     * 引数の商品の要素がマスターになかったら追加する
     *
     * @param item
     * @return
     */
    public ItemMaster absolveItem(Item item) {
        if (!this.getTeam_id().equals(item.getTeam_id())) {
            List<String> master = List.of(this.getTeam_id().split(","));
            List<String> itemTeamId = List.of(item.getTeam_id().split(","));

            if (itemTeamId.size() > master.size()) {
                for (String teamId : itemTeamId) {
                    if (!master.contains(teamId)) {
                        master.add(teamId);
                    }
                }
            }
            this.setTeam_id(String.join(",", master));
        }

        // 画像が空だったら追加する
        if (item.getImage1() != null) {
            boolean hasNext1 = this.fillBlankImage(item.getImage1());

            if (item.getImage2() != null && hasNext1) {
                boolean hasNext2 = this.fillBlankImage(item.getImage2());

                if (item.getImage3() != null && hasNext2) {
                    this.fillBlankImage(item.getImage3());
                }
            }
        }

        if (this.getItem_caption() != null && this.getItem_caption().length() < item.getItem_caption().length()) {
            this.setItem_caption(item.getItem_caption());
        }

        if ((this.getMember_id() == null && item.getMember_id() != null) ||(this.getMember_id() != null && item.getMember_id() != null && !this.getMember_id().equals(item.getMember_id()))) {
            List<String> master = List.of(this.getMember_id().split(","));
            List<String> itemMemberId = List.of(item.getMember_id().split(","));

            if (itemMemberId.size() > master.size()) {
                for (String memberId : itemMemberId) {
                    if (!master.contains(memberId)) {
                        master.add(memberId);
                    }
                }
            }
            this.setMember_id(master.stream().collect(Collectors.joining(",")));
        }
        return this;
    }

    public Integer[] getTags() {
        // タグを作成（チーム名のみ）
        List<Integer> tmpList = new ArrayList<>();
        Integer[] teamArr = new Integer[0];

        if (StringUtils.hasText(this.getTeam_id())) {
            List.of(this.getTeam_id().split(",")).forEach(e -> tmpList.add(WpTagEnum.getByDbTeamId(e)));
        }

        if (tmpList.size() > 0) {
            teamArr = tmpList.toArray(new Integer[tmpList.size()]);
        }
        return teamArr;
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
            if (this.getImage2() == null || this.getImage3() == null) {
                return true;
            } else {
                return false;
            }
        } else if (this.getImage2() == null) {
            // image2が空だったら入れる
            this.setImage2(imageUrl);
            return true;
        } else if (this.getImage2().equals(imageUrl)) {
            // image2と引数が同じ値だったら、image3に入れてはいけないのでimage3が空かどうかでreturn値を決める
            if (this.getImage3() == null) {
                return true;
            } else {
                return false;
            }
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
        } else if (!StringUtils.hasText(this.getImage3())) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * チームID("n,n,n,n,n")をLongListにして返します。
     *
     * @return
     */
    public List<Long> getTeamIdList() {
        if (this.getTeam_id() != null && this.getTeam_id().contains(",")) {
            return List.of(this.getTeam_id().split(",")).stream().map(Integer::parseInt).collect(Collectors.toList()).stream().map(Integer::longValue).collect(Collectors.toList());
        } else if (StringUtils.hasText(this.getTeam_id())) {
            List<Long> teamIdList = new ArrayList<>();
            teamIdList.add((long)Integer.parseInt(this.getTeam_id()));
            return teamIdList;
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * チームID("n,n,n,n,n")をLongListにして返します。
     *
     * @return
     */
    public List<Long> getMemberIdList() {
        if (this.getMember_id() != null && this.getMember_id().contains(",")) {
            return List.of(this.getMember_id().split(",")).stream().map(Integer::parseInt).collect(Collectors.toList()).stream().map(Integer::longValue).collect(Collectors.toList());
        } else if (StringUtils.hasText(this.getMember_id())) {
            List<Long> memberIdList = new ArrayList<>();
            memberIdList.add((long)Integer.parseInt(this.getMember_id()));
            return memberIdList;
        } else {
            return new ArrayList<>();
        }
    }

    public ItemMaster adjustedCopy() {
        ItemMaster newItemMaster = new ItemMaster();
        BeanUtils.copyProperties(this, newItemMaster);
        newItemMaster.setItem_m_id(null);
        newItemMaster.setFct_chk(false);
        newItemMaster.setWp_id(null);
        newItemMaster.setCreated_at(null);
        newItemMaster.setUpdated_at(null);
        return newItemMaster;
    }
}
