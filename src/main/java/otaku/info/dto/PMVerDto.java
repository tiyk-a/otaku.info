package otaku.info.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * PMverのフロント用DTO
 *
 */
@Data
@AllArgsConstructor
public class PMVerDto {

    private Long pm_v_id;

    private LocalDateTime on_air_date;

    private Long station_id;

    private String station_name;

    private Boolean del_flg;
}
