package otaku.info.form;

import lombok.Data;

import java.util.List;

/**
 * Program -> PM登録のためにフロントから使うフォーム
 * 1 programずつ。
 *
 */
@Data
public class PMVerForm {

    private Long program_id;

    private Long pm_id;

    /** 週次とかの番組の場合 */
    private Long regular_pm_id;

    private Long teamId;

    // [prelId, programId, teamId, pmrelですかフラグ(1=true)]
    private List<List<Integer>> pmrel;

    // [prelMId, prelId, memberId, pmrelMですかフラグ(1=true)]
    private List<List<Integer>> pmrelm;

    private String title;

    private String description;

    private String on_air_date;

    private Long station_id;

    private boolean del_flg;
}
