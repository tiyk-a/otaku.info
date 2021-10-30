package otaku.info.form;

import lombok.Data;

import java.util.Date;

@Data
public class IMVerForm {

    private Long im_id;

    private Long teamId;

    private String title;

    private Long wp_id;

    private Date publication_date;

    private boolean del_flg;

    private String[] verArr;
}
