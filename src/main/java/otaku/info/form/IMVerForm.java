package otaku.info.form;

import lombok.Data;
import org.json.JSONArray;
import otaku.info.entity.ImVer;

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

    private boolean del_flg;

    private String[] verArr;
}
