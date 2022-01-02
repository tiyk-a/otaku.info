package otaku.info.form;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PForm {

    /** program_id */
    private Long id;

    private String title;

    private LocalDateTime on_air_date;

    /** 説明文 */
    private String description;
}
