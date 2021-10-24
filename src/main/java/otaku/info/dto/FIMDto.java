package otaku.info.dto;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Front Item Master Dto
 * chiharu-front(React.js)で使用するItemMasterのDTOです。
 * Teamごとに画面表示してる。Wpidとか合わせて表示したいからItemMasterだけだと不足するので。
 */
@Getter
@Setter
public class FIMDto {

    private Long item_m_id;

    private String title;

    private String item_caption;

    private Date publication_date;

    private Integer price;

    /** WpIdはimrelから取得。1つのitemmasterでもteamによってここが変わるのよ */
    private Long wp_id;

    private boolean fct_chk;

    private boolean del_flg;

    private Long merge_im_id;

    private Timestamp created_at;

    private Timestamp updated_at;
}
