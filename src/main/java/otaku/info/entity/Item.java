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

    public WpDto convertToWpDto() {
        WpDto wpDto = new WpDto();
        wpDto.setTitle(this.getTitle());
        wpDto.setContent(this.getItem_caption());
        wpDto.setExcerpt(this.getTitle());

        if (!StringUtils.hasText(this.getTeam_id())) {
            return null;
        }

        List<Integer> tmpList = new ArrayList<>();
        Integer[] teamArr = new Integer[0];
        List.of(this.getTeam_id().split(",")).forEach(e -> tmpList.add(WpTagEnum.getByDbTeamId(e)));
        if (tmpList.size() > 0) {
            teamArr = tmpList.toArray(new Integer[tmpList.size()]);
        }

        // TODO: memberIdのタグ設定は今後行う
//        String[] memberArr = new String[0];
//        if (StringUtils.hasText(this.getMember_id())) {
//            memberArr = this.getMember_id().split(",");
//        }

//        String[] tags = new String[teamArr.length + memberArr.length];
//        System.arraycopy(teamArr, 0, tags, 0, teamArr.length);
//        System.arraycopy(memberArr, 0, tags, teamArr.length, memberArr.length);
//        wpDto.setTags(tags);

        wpDto.setTags(teamArr);
        return wpDto;
    }
}
