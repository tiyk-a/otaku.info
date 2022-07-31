package otaku.info.form;

import lombok.Data;
import java.util.Date;

/**
 * 商品画像以外の商品データを登録するフォーム
 *
 * @author hasegawachiharu
 */
@Data
public class IMForm {

    private Long im_id;

    private String url;

    private String title;

    private Long wp_id;

    private String item_caption;

    private String publication_date;

    private Integer price;

    private boolean fct_chk;

    private boolean del_flg;

    private String amazon_image;
}