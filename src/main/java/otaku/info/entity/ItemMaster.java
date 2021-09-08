package otaku.info.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import otaku.info.dto.WpDto;
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
     * WpDtoに変換します。
     *
     * @return
     */
    public WpDto convertToWpDto() {
        WpDto wpDto = new WpDto();
        wpDto.setTitle(this.getTitle());
        // itemのカテゴリ5を登録
        wpDto.setCategories(new Integer[]{5});
        wpDto.setContent(this.getItem_caption());
        wpDto.setPath("posts");
        wpDto.setExcerpt(this.getTitle());

        // タグを作成（チーム名のみ）
        List<Integer> tmpList = new ArrayList<>();
        Integer[] teamArr = new Integer[0];

        if (StringUtils.hasText(this.getTeam_id())) {
            List.of(this.getTeam_id().split(",")).forEach(e -> tmpList.add(WpTagEnum.getByDbTeamId(e)));
        }

        if (tmpList.size() > 0) {
            teamArr = tmpList.toArray(new Integer[tmpList.size()]);
        }

        wpDto.setTags(teamArr);
        return wpDto;
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
            this.setTeam_id(master.stream().collect(Collectors.joining(",")));
        }

        if (this.getImage1() == null && item.getImage1() != null) {
            this.setImage1(item.getImage1());
        }

        if (StringUtils.hasText(this.getImage1()) && this.getImage2() == null && item.getImage1() != null) {
            if (StringUtils.hasText(item.getImage1()) && !this.getImage1().equals(item.getImage1())) {
                this.setImage2(item.getImage1());
            } else if (StringUtils.hasText(item.getImage2()) && !this.getImage1().equals(item.getImage2())) {
                this.setImage2(item.getImage2());
            }
        }

        if (StringUtils.hasText(this.getImage1()) && StringUtils.hasText(this.getImage2()) && this.getImage3() == null && item.getImage1() != null) {
            if (!this.getImage1().equals(item.getImage1()) && !this.getImage2().equals(item.getImage1())) {
                this.setImage3(item.getImage1());
            } else if (!this.getImage1().equals(item.getImage2()) && !this.getImage2().equals(item.getImage2())) {
                this.setImage3(item.getImage2());
            } else if (!this.getImage1().equals(item.getImage3()) && !this.getImage2().equals(item.getImage3())) {
                this.setImage3(item.getImage3());
            }
        }

        if (this.getItem_caption() != null && this.getItem_caption().length() < item.getItem_caption().length()) {
            this.setItem_caption(item.getItem_caption());
        }

        if (!this.getMember_id().equals(item.getMember_id())) {
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
}
