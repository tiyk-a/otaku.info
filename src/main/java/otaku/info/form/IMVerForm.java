package otaku.info.form;

import lombok.Data;

import java.util.List;

@Data
public class IMVerForm {

    private Long item_id;

    private Long im_id;

    private Long teamId;

    private String title;

    private String note;

    private String teamArr;

    private String memArr;

    private Long wp_id;

    private String publication_date;

    private String amazon_image;

    private boolean del_flg;

    // IM verを取得してくる[[im_v_id, ver_name, im_id]]
    private List<String[]> vers;
}
