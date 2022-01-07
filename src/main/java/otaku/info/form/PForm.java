package otaku.info.form;

import lombok.Data;

import java.util.List;


@Data
public class PForm {

    /** program_id */
    private Long id;

    private String title;

    /** 日付の受け取りはString,"yyyy-MM-dd HH:mm"の形で送りたい */
    private String on_air_date;

    /** 説明文 */
    private String description;

    private List<Long[]> prel;
}
