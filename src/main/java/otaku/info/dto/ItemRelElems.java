package otaku.info.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemRelElems {

    private Long item_id;
    private Long item_m_id;
    private Long team_id;
    private Long member_id;
    private Long wp_id;
}
