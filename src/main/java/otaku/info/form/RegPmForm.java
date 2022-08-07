package otaku.info.form;

import lombok.Data;

/**
 * Regular_PM登録に使うフォーム
 *
 */
@Data
public class RegPmForm {

    private Long regular_pm_id;

    private String teamArr;

    private String memArr;

    private String title;

    private String description;

    private String start_date;

    private String end_date;

}
