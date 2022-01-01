package otaku.info.form;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class IMVerForm {

    private Long item_id;

    private Long im_id;

    private Long teamId;

    private String title;

    private Long wp_id;

    private Date publication_date;

    private String amazon_image;

    private boolean del_flg;

    // IM verをオブジェクトで取得してくる
    private List<String[]> vers;
}
