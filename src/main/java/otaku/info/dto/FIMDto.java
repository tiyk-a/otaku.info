package otaku.info.dto;

import lombok.Getter;
import lombok.Setter;
import otaku.info.entity.IM;
import otaku.info.entity.IMRel;
import otaku.info.entity.IMRelMem;
import otaku.info.entity.ImVer;

import java.sql.Timestamp;
import java.util.List;

/**
 * Front Item Master Dto
 * chiharu-front(React.js)で使用するItemMasterのDTOです。
 * Teamごとに画面表示してる。Wpidとか合わせて表示したいからItemMasterだけだと不足するので。
 */
@Getter
@Setter
public class FIMDto {

    private IM im;

    private List<IMRel> relList;

    private List<IMRelMem> relMemList;

    private List<ImVer> verList;

    // TODO: この項目使ってなくない？
    private Long merge_im_id;

    private Timestamp created_at;

    private Timestamp updated_at;
}
