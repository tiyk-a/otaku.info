package otaku.info.dto;

import lombok.Data;
import otaku.info.entity.*;

import java.util.List;
import java.util.Map;

/**
 * レギュラー番組のデータを一括で取得します
 */
@Data
public class RegPMDto {

    private RegularPM regularPM;

    /**
     * CastからIDだけ引き抜いて格納
     */
    private List<Long> castList;

    /**
     * RegPmStationからIDだけ引き抜いて格納
     */
    private Map<Long, String> stationMap;
}
