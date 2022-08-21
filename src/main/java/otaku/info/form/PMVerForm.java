package otaku.info.form;

import lombok.Data;

/**
 * Program -> PM登録のためにフロントから使うフォーム
 * 1 programずつ。
 *
 */
@Data
public class PMVerForm {

    private Long program_id;

    private Long pm_id;

//    /** 週次とかの番組の場合 */
//    private Long regular_pm_id;

    private String teamArr;

    private String memArr;

    private String title;

    private String description;

    /**
     * v_id: e.pm_v_id,
     * on_air_date: e.on_air_date,
     * station_name: e.station_name,
     * del_flg: e.del_flg
     */
    private Object[] verList;

    /**
     * verに登録される
     */
    private String on_air_date;

    private Long station_id;

    private boolean del_flg;

}
